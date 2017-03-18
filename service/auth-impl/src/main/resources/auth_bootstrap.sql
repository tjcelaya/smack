-- drop database auth;
create database if not exists auth;
use auth;
DROP TABLE IF EXISTS oauth_scope;
CREATE TABLE oauth_scope (
    id varchar(40) NOT NULL,
    description varchar(255) NOT NULL,
    PRIMARY KEY (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_grant;
CREATE TABLE oauth_grant (
    id varchar(40) NOT NULL,
    PRIMARY KEY (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_grant_scope;
CREATE TABLE oauth_grant_scope (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    grant_id varchar(40) NOT NULL,
    scope_id varchar(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_grant_scope_grant_id_idx (grant_id),
    INDEX oauth_grant_scope_scope_id_idx (scope_id),
    CONSTRAINT oauth_grant_scope_grant_id_fk
        FOREIGN KEY (grant_id)
        REFERENCES oauth_grant (id)
        ON DELETE CASCADE,
    CONSTRAINT oauth_grant_scope_scope_id_fk
        FOREIGN KEY (scope_id)
        REFERENCES oauth_scope (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_client;
CREATE TABLE oauth_client (
    id varchar(40) NOT NULL,
    secret varchar(40) NOT NULL,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY oauth_client_id_secret_unique_idx (id, secret)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_client_endpoint;
CREATE TABLE oauth_client_endpoint (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    client_id varchar(40) NOT NULL,
    redirect_uri varchar(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY oauth_client_endpoint_client_id_redirect_uri (client_id, redirect_uri),
    CONSTRAINT oauth_client_endpoint_client_id_fk
        FOREIGN KEY (client_id)
        REFERENCES oauth_client (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_client_scope;
CREATE TABLE oauth_client_scope (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    client_id varchar(40) NOT NULL,
    scope_id varchar(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_client_scope_client_id_idx (client_id),
    INDEX oauth_client_scope_scope_id_idx (scope_id),
    CONSTRAINT oauth_client_scope_client_id_fk
        FOREIGN KEY (client_id)
        REFERENCES oauth_client (id)
        ON DELETE CASCADE,
    CONSTRAINT oauth_client_scope_scope_id_fk
        FOREIGN KEY (scope_id)
        REFERENCES oauth_scope (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_client_grant;
CREATE TABLE oauth_client_grant (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    client_id varchar(40) NOT NULL,
    grant_id varchar(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_client_grant_client_id_idx (client_id),
    INDEX oauth_client_grant_grant_id_idx (grant_id),
    CONSTRAINT oauth_client_grant_client_id_fk
        FOREIGN KEY (client_id)
        REFERENCES oauth_client (id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT oauth_client_grant_grant_id_fk
        FOREIGN KEY (grant_id)
        REFERENCES oauth_grant (id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_session;
CREATE TABLE oauth_session (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    client_id varchar(40) NOT NULL,
    owner_type enum('client','user') NOT NULL,
    owner_id varchar(255) NOT NULL,
    client_redirect_uri varchar(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_session_client_id_owner_type_owner_id_idx (client_id, owner_type, owner_id),
    CONSTRAINT oauth_session_client_id_fk
        FOREIGN KEY (client_id)
        REFERENCES oauth_client (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_session_scope;
CREATE TABLE oauth_session_scope (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    session_id BIGINT UNSIGNED,
    scope_id varchar(40)  NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_session_scope_session_id_idx (session_id),
    INDEX oauth_session_scope_scope_id_idx (scope_id),
    CONSTRAINT oauth_session_scope_session_id_fk
        FOREIGN KEY (session_id)
        REFERENCES oauth_session (id)
        ON DELETE CASCADE,
    CONSTRAINT oauth_session_scope_scope_id_fk
        FOREIGN KEY (scope_id)
        REFERENCES oauth_scope (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_auth_code;
CREATE TABLE oauth_auth_code (
    id varchar(40) NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL,
    redirect_uri varchar(255) NOT NULL,
    expire_time INT(11) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX session_id_idx (session_id),
    CONSTRAINT oauth_auth_code_session_id_fk
        FOREIGN KEY (session_id)
        REFERENCES oauth_session (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_auth_code_scope;
CREATE TABLE oauth_auth_code_scope (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    auth_code_id varchar(40) NOT NULL,
    scope_id varchar(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_auth_code_scope_auth_code_id_idx (auth_code_id),
    INDEX oauth_auth_code_scope_scope_id_idx (scope_id),
    CONSTRAINT oauth_auth_code_scope_auth_code_id_fk
        FOREIGN KEY (auth_code_id)
        REFERENCES oauth_auth_code (id)
        ON DELETE CASCADE,
    CONSTRAINT oauth_auth_code_scope_scope_id_fk
        FOREIGN KEY (scope_id)
        REFERENCES oauth_scope (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_access_token;
CREATE TABLE oauth_access_token (
    id varchar(40) NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL,
    expire_time INT(11) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY oauth_access_token_id_session_id_idx (id, session_id),
    INDEX oauth_access_token_session_id_idx (session_id),
    CONSTRAINT oauth_access_token_session_id_fk
        FOREIGN KEY (session_id)
        REFERENCES oauth_session (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_access_token_param_map;
CREATE TABLE oauth_access_token_param_map (
    access_token_id varchar(40) NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL,
    params TINYTEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_access_token_scope;
CREATE TABLE oauth_access_token_scope (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    access_token_id varchar(40) NOT NULL,
    scope_id varchar(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX oauth_access_token_scope_access_token_id_idx (access_token_id),
    INDEX oauth_access_token_scope_scope_id_idx (scope_id),
    CONSTRAINT oauth_access_token_scope_access_token_id_fk
        FOREIGN KEY (access_token_id)
        REFERENCES oauth_access_token (id)
        ON DELETE CASCADE,
    CONSTRAINT oauth_access_token_scope_scope_id_fk
        FOREIGN KEY (scope_id)
        REFERENCES oauth_scope (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
DROP TABLE IF EXISTS oauth_refresh_token;
CREATE TABLE oauth_refresh_token (
    id varchar(40) NOT NULL,
    access_token_id varchar(40) NOT NULL,
    expire_time INT(11) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (access_token_id),
    INDEX oauth_refresh_token_id_idx (id),
    CONSTRAINT oauth_refresh_token_access_token_id_fk
        FOREIGN KEY (access_token_id)
        REFERENCES oauth_access_token (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- --
CREATE TABLE oauth_owner (
    id varchar(255) NOT NULL,
    owner_type enum('client','user') NOT NULL,
    name varchar(255) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO oauth_grant (id)
VALUES ('authorization_code'),('client_credentials'),('password'),('refresh_token');