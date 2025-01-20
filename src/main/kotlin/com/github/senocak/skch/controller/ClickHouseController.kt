package com.github.senocak.skch.controller

import com.clickhouse.client.api.Client
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader
import com.clickhouse.client.api.internal.BasicObjectsPool
import com.clickhouse.client.api.internal.CachingObjectsSupplier
import com.clickhouse.client.api.metadata.TableSchema
import com.clickhouse.client.api.metrics.ClientMetrics
import com.clickhouse.client.api.query.QueryResponse
import com.clickhouse.client.api.query.QuerySettings
import com.clickhouse.data.ClickHouseFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.nanoseconds

class VirtualDatasetRecord{
    var id: UUID? = null
    var p1: Long = 0
    var number: BigInteger? = null
    var p2: Float = 0f
    var p3: Double = 0.0
}
data class CalculationResult(var p1: Long)

// https://github.com/ClickHouse/clickhouse-java/blob/main/examples/client-v2/src/main/java/com/clickhouse/examples/client_v2/Main.java
// implementation("com.clickhouse:client-v2:0.7.2")
@RestController
@RequestMapping("/api/v1/clickhouse")
class ClickHouseController(
    private val chDirectClient: Client
): SmartLifecycle {
    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private var running = false
    private lateinit var datasetQuerySchema: TableSchema
    private lateinit var pool: BasicObjectsPool<CachingObjectsSupplier<VirtualDatasetRecord>>
    private val jsonMapper = JsonMapper()

    override fun start() {
        running = true
        chDirectClient.ping(3000); // helps to warm up the connection
        // Register class for deserialization
        datasetQuerySchema = chDirectClient.getTableSchemaFromQuery(DATASET_QUERY)
        chDirectClient.register(VirtualDatasetRecord::class.java, datasetQuerySchema)
        log.info("Dataset schema: ${datasetQuerySchema.columns}")
        pool = object : BasicObjectsPool<CachingObjectsSupplier<VirtualDatasetRecord>>(
            ConcurrentLinkedDeque<CachingObjectsSupplier<VirtualDatasetRecord>>(), 100
        ) {
            override fun create(): CachingObjectsSupplier<VirtualDatasetRecord> {
                return object : CachingObjectsSupplier<VirtualDatasetRecord>(LinkedList(), 100) {
                    override fun create(): VirtualDatasetRecord {
                        return VirtualDatasetRecord()
                    }
                }
            }
        }
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean = running

    /**
     * Fetches data from a DB using row binary reader. VirtualDatasetRecord objects are created on each iteration and
     * filled with data from the reader. Gives full control on how data is processed and stored.
     * If this method returns a lot of data it may cause application slowdown.
     *
     * @param limit number of records to fetch
     * @return list of VirtualDatasetRecord objects
     */
    @GetMapping("/dataset/reader")
    fun directDatasetFetch(@RequestParam(required = false) limit: Int = 100): List<VirtualDatasetRecord> {
        try {
            chDirectClient.query("$DATASET_QUERY LIMIT $limit")[3000, TimeUnit.MILLISECONDS].use { response: QueryResponse ->
                val result = ArrayList<VirtualDatasetRecord>()
                // iterable approach is more efficient for large datasets because it doesn't load all records into memory
                val reader: ClickHouseBinaryFormatReader = chDirectClient.newBinaryFormatReader(response)
                val duration: Long = measureTimeMillis {
                    while (reader.next() != null) {
                        result.add(element =
                            VirtualDatasetRecord().also {
                                it.id = reader.getUUID("id")
                                it.p1 = reader.getLong("p1")
                                it.number = reader.getBigInteger("number")
                                it.p2 = reader.getFloat("p2")
                                it.p3 = reader.getDouble("p3")
                            }
                        )
                    }
                }
                // report metrics (only for demonstration purposes)
                log.info("records: ${result.size}, read time: ${duration.nanoseconds} ms, client time: ${response.metrics.getMetric(ClientMetrics.OP_DURATION).long} ms, server time: ${TimeUnit.NANOSECONDS.toMillis(response.serverTime)} ms")
                return result
            }
        } catch (e: Exception) {
            log.error("Failed to fetch dataset: ${e.message}")
            throw RuntimeException("Failed to fetch dataset", e)
        }
    }

    /**
     * Reads data in JSONEachRow format, parses it into JSON library object (can be used for further processing) and writes it back to the response.
     * This helps to reduce effort of writing data to the response.
     *
     * @param httpResp response object
     * @param limit number of records to fetch
     */
    @GetMapping("/dataset/json_each_row_in_and_out")
    @ResponseBody
    fun directDataFetchJSONEachRow(
        httpResp: HttpServletResponse,
        @RequestParam(required = false) limit: Int = 100
    ) {
        val settings: QuerySettings = QuerySettings().setFormat(ClickHouseFormat.JSONEachRow)
        try {
            chDirectClient.query("$DATASET_QUERY LIMIT $limit", settings).get(3000, TimeUnit.MILLISECONDS).use { response: QueryResponse ->
                jsonMapper.readerFor(ObjectNode::class.java)
                    .readValues<ObjectNode>(response.inputStream).use { jsonIter: MappingIterator<ObjectNode> ->
                        httpResp.contentType = "application/json"
                        val jsonGen: JsonGenerator = jsonMapper.factory.createGenerator(httpResp.outputStream)
                        jsonGen.writeStartArray()
                        var counter = 0
                        val duration: Long = measureTimeMillis {
                            while (jsonIter.hasNext()) {
                                val node: ObjectNode = jsonIter.next()
                                // here may be some processing logic
                                node.put("ordNum", counter++)
                            }
                            jsonGen.writeEndArray()
                            jsonGen.close()
                        }
                        // report metrics (only for demonstration purposes)
                        log.info("records: $counter, read time: ${duration.nanoseconds} ms, client time: ${response.metrics.getMetric(ClientMetrics.OP_DURATION).long} ms, server time: ${TimeUnit.NANOSECONDS.toMillis(response.serverTime)} ms")
                    }
            }
        } catch (e: Exception) {
            log.error("Failed to fetch dataset: ${e.message}")
            throw RuntimeException("Failed to fetch dataset", e)
        }
    }

    /**
     * Using POJO deserialization to fetch data from ClickHouse.
     * If cache is enabled, objects are reused from the pool otherwise new objects are created on each iteration.
     *
     * @param limit number of records to fetch
     * @return sum of p1 field
     */
    @GetMapping("/dataset/read_to_pojo")
    fun directDatasetReadToPojo(
        @RequestParam(name = "limit", required = false) limit: Int = 100,
        @RequestParam(name = "cache", required = false) cache: Boolean = false
    ): CalculationResult {
        var result: List<VirtualDatasetRecord>
        val objectsPool: Supplier<VirtualDatasetRecord> = when {
            cache -> pool.lease()
            else -> Supplier<VirtualDatasetRecord> { VirtualDatasetRecord() }
        }
        try {
            val duration: Long = measureTimeMillis {
                result = chDirectClient.queryAll("$DATASET_QUERY LIMIT $limit",
                    VirtualDatasetRecord::class.java, datasetQuerySchema, objectsPool)
            }
            log.info("records: ${result.size}, read time: ${duration.nanoseconds} ms")
            var p1Sum: Long = 0
            result.forEach { record: VirtualDatasetRecord -> p1Sum += record.p1 }
            if (cache)
                (objectsPool as CachingObjectsSupplier<VirtualDatasetRecord>).reset()
            return CalculationResult(p1 = p1Sum)
        } catch (e: Exception) {
            log.error("Failed to fetch dataset: ${e.message}")
            throw RuntimeException("Failed to fetch dataset", e)
        } finally {
            if (cache) {
                pool.release(objectsPool as CachingObjectsSupplier<VirtualDatasetRecord>)
            }
        }
    }

    companion object {
        /**
         * Makes query to a `system.numbers` that can be used to generate a virtual dataset.
         * Size of the dataset is limited by the `limit` parameter.
         */
        private const val DATASET_QUERY: String = """
            SELECT generateUUIDv4() as id,
                toUInt32(number) as p1,
                number,
                toFloat32(number/100000) as p2,
                toFloat64(number/100000) as p3
                FROM system.numbers
            """
    }
}
