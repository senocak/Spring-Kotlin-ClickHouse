INSERT INTO testcontainer.roles (created_at, updated_at, id, name)
    VALUES ('2025-01-17 17:40:21.179000', '2025-01-17 17:40:21.179000', '38bd0085-add5-4b06-871f-c419dd3b8ae0', 'ROLE_ADMIN');
INSERT INTO testcontainer.roles (created_at, updated_at, id, name)
    VALUES ('2025-01-17 17:40:21.157000', '2025-01-17 17:40:21.157000', 'ce0a3abf-c603-42d4-9653-5b88612e5987', 'ROLE_USER');

INSERT INTO testcontainer.users (created_at, updated_at, id, name, email, password)
    VALUES ('2025-01-17 17:40:21.540000', '2025-01-17 17:40:21.540000', '081c4afb-51d8-4127-941c-72bf9b92f7a4', 'anil3', 'anil3@gmail.com', '$2a$10$ykFezMrgYUi8ZAyNCnLZyO2d9OgU/Uh0jzxtCAce6rR1wkzFw.azm');
INSERT INTO testcontainer.users (created_at, updated_at, id, name, email, password)
    VALUES ('2025-01-17 17:40:21.286000', '2025-01-17 17:40:21.286000', '8089e918-66d5-45e5-9311-fe56839bd814', 'anil1', 'anil1@senocak.com', '$2a$10$dRsXkZBdwF0TS4V4TVSKPudVpoqNgATS91/Sw/talFevy/gbfUy4e');
INSERT INTO testcontainer.users (created_at, updated_at, id, name, email, password)
    VALUES ('2025-01-17 17:40:21.420000', '2025-01-17 17:40:21.420000', '2250c69e-9826-40ba-be3a-756ddf0c99aa', 'anil2', 'anil2@gmail.com', '$2a$10$62VDAzD/qmls.f73mcH7JOUCOxQReESqxWDKZdxrxUPyy42WSO7.K');

INSERT INTO testcontainer.user_roles (role_id, user_id)
    VALUES ('38bd0085-add5-4b06-871f-c419dd3b8ae0', '8089e918-66d5-45e5-9311-fe56839bd814');
INSERT INTO testcontainer.user_roles (role_id, user_id)
    VALUES ('ce0a3abf-c603-42d4-9653-5b88612e5987', '8089e918-66d5-45e5-9311-fe56839bd814');
INSERT INTO testcontainer.user_roles (role_id, user_id)
    VALUES ('ce0a3abf-c603-42d4-9653-5b88612e5987', '081c4afb-51d8-4127-941c-72bf9b92f7a4');
INSERT INTO testcontainer.user_roles (role_id, user_id)
    VALUES ('ce0a3abf-c603-42d4-9653-5b88612e5987', '2250c69e-9826-40ba-be3a-756ddf0c99aa');
