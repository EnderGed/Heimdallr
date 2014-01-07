drop table bombs;
drop table in_game;
drop table games;
drop table users;

CREATE TABLE users(
   login VARCHAR(20) PRIMARY KEY,
   pass bytea NOT NULL,
   e_mail VARCHAR(100) NOT NULL
);


CREATE TABLE games(
   created_by VARCHAR(20) PRIMARY KEY references users(login),
   ID VARCHAR(10)
);

CREATE TABLE in_game(
   login VARCHAR(20) references users(login),
   game VARCHAR(20) references games(created_by) default NULL,
   connected boolean default TRUE
);

CREATE TABLE bombs(
   login VARCHAR(20) references users(login),
   bomb_limit int default 3,
   bombs_in_inventory int default 3,
   bomb_radius int default 100
);

insert into users values
('kasia', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com'),
('monika', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com'),
('weronika', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com'),
('magda', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com'), ('sylwia', 'U\251\364\370\231K\033\277 X\3528\310\357\266\304Y\000\010\024\325\363\234\010p\002W\0269\346#\016', 'aleksandra92mielcarek@gmail.com');
insert into bombs values('kasia'), ('monika'), ('weronika'), ('magda'), ('sylwia');

select * from users;
select * from bombs;
select * from in_game;
delete from in_game;
delete from games;
select * from games;