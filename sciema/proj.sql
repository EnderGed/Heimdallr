drop table if exists bombs;
drop table if exists in_game;
drop table if exists games;
drop table if exists users;

CREATE TABLE users(
   login VARCHAR(20) PRIMARY KEY,
   pass bytea NOT NULL,
   pass_temp boolean default TRUE,
   e_mail VARCHAR(100) NOT NULL
);


CREATE TABLE games(
   created_by VARCHAR(20) PRIMARY KEY references users(login),
   ID int
);

CREATE TABLE in_game(
   login VARCHAR(20) references users(login) unique,
   game VARCHAR(20) references games(created_by) default NULL,
   connected boolean default TRUE
);

CREATE TABLE bombs(
   login VARCHAR(20) references users(login) UNIQUE,
   bomb_limit int default 3,
   bombs_in_inventory int default 3,
   bomb_radius int default 100
);


CREATE OR REPLACE FUNCTION game_del() RETURNS TRIGGER AS $$
BEGIN
   UPDATE in_game SET game = NULL WHERE game = OLD.created_by;
   RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION bomb_ins() RETURNS TRIGGER AS $$
BEGIN
   INSERT INTO bombs(login) VALUES (NEW.login);
   RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER bomb_insert AFTER INSERT ON users FOR EACH ROW EXECUTE PROCEDURE bomb_ins();
CREATE TRIGGER game_delete BEFORE DELETE ON games FOR EACH ROW EXECUTE PROCEDURE game_del();

insert into users(login, pass, e_mail) values
('kasia', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com'),
('marta', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com'),
('bartek', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com');
