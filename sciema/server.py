from socket import *
from email.mime.text import MIMEText
import random
import codecs
import threading
import postgresql
import smtplib
import string
import re
from Crypto.Hash import SHA256
from protocol import *
from user import User
from gra import *
from gra_klasy import Player
import sys
from struct import pack

e_mail = re.compile(r"[^@]+@[^@]+\.[^@]+")

myfile = open ("D:\\folder\\pwr\\projekt\\mail.txt", encoding="ascii", errors="ignore")	
registration_mail=myfile.read()
myfile.close()

myfile = open ("D:\\folder\\pwr\\projekt\\noweHaslo.txt", encoding="ascii", errors="ignore")	
change_pass_mail=myfile.read()
myfile.close()

db = postgresql.open("pq://postgres:agnieszka1@localhost/uzytkownicy")
sql_login = db.prepare("SELECT * FROM users WHERE login LIKE $1")
sql_address = db.prepare("SELECT * FROM users WHERE e_mail LIKE $1")
sql_check_login = db.prepare("SELECT * FROM users WHERE login LIKE $1 AND pass LIKE $2")
sql_update_pass = db.prepare("UPDATE users SET pass = $1 WHERE login LIKE $2")
sql_find_login = db.prepare("SELECT login FROM users WHERE e_mail LIKE $1")
sql_registration1 = db.prepare("INSERT INTO users(login,pass,e_mail) VALUES ($1, $2, $3)")
sql_bomb = db.prepare("SELECT bomb_limit, bombs_in_inventory, bomb_radius FROM bombs WHERE login LIKE $1")
sql_in_game = db.prepare("SELECT login FROM in_game WHERE login LIKE $1 AND connected = TRUE")
sql_add_to_game = db.prepare("UPDATE in_game SET connected = TRUE WHERE login LIKE $1")
sql_rmv_from_game = db.prepare("UPDATE in_game SET connected = FALSE WHERE login LIKE $1")
sql_logout = db.prepare("DELETE FROM in_game WHERE login LIKE $1")
sql_new_player = db.prepare("INSERT INTO in_game VALUES ($1)")
sql_add_game = db.prepare("UPDATE in_game SET game = $1 WHERE login LIKE $2")
sql_set_game_null = db.prepare("UPDATE in_game SET game = NULL WHERE login LIKE $1")
sql_new_game = db.prepare("INSERT INTO games VALUES($1, $2)")
sql_rmv_game = db.prepare("DELETE FROM games WHERE ID = $1")
sql_pass_temp = db.prepare("SELECT * FROM users WHERE login = $1 AND pass_temp = TRUE")
sql_set_temp_pass1 = db.prepare("UPDATE users SET pass_temp = FALSE WHERE login = $1")
sql_set_temp_pass2 = db.prepare("UPDATE users SET pass_temp = TRUE WHERE login = $1")

def password_gen(size=8, chars=string.ascii_uppercase + string.digits):
	return ''.join(random.choice(chars) for x in range(size))
		
class Doorkeeper(threading.Thread):
	'''
	This class is server main thread. It waits for users. When someone try to connect,
	Doorkeeper accept user
	'''
	def __init__(self, server):
		threading.Thread.__init__(self)
		self.server = server
	
	def run(self):
		while True:
			if len(self.server.get_users()) < self.server.max_users:
				try:
					conn, addr = self.server.s.accept()
					self.server.new_user(conn)
				except OSError:
					print("nie udalo sie")
					sys.exit()
		
class Server:
	'''
	This is Server main class. It starts TCP socket
	'''
	
	def __init__(self):
		self.HOST = ''
		self.PORT = 5657
		self._users = []
		self._games = []
		self.max_users = 5
		self.max_games = 2
		self.s = None 
		self.__first()
		self.prot = Protocol(self)
		self.lokaj = Doorkeeper(self)
		self.lokaj.start()
	
	
	def get_games(self): return self._games	
	
	def rm_add_game(self, game, b):
		if b:
			self._games.remove(game)
		else:
			self._games.append(game)
	
	def _send_mail(self, mail, login, address, password):
		'''
		This function send e-mail to user
		arguments:
		mail - it is a message, which server send to the user's e-mail address
		login - user's login
		address - address e-mail on which will be send message
		password - password generated by password_gen
		'''
		SMTP_server = smtplib.SMTP('smtp.gmail.com', 587)
		sender = 'heimdallrgame@gmail.com'
		SMTP_server.ehlo()
		SMTP_server.starttls()
		SMTP_server.login(sender, "projektgry")
		receivers = [address]
		w = mail
		w = w.replace("To: ", "To: <" + receivers[0] + ">")
		w = w.replace("Login: ", "Login: " + login)
		w = w.replace("Haslo: ", "Haslo: " + password)
		SMTP_server.sendmail(sender, receivers, w)
	
	def get_users(self): return self._users
	
	def __first(self):
		'''
		In this function starts server
		'''
		try: 
			self.s = socket(AF_INET, SOCK_STREAM) 
			self.s.bind((self.HOST,self.PORT)) 
			self.s.listen(5)
			print('Server started')
		except OSError:
			return
	
	def rm_add_user(self, user, b):
		if b:
			self._users.remove(user)
		else:
			self._users.append(user)
	
	def _get_hash(self, str):
		'''
		This function generate SHA256 from given string
		'''
		hash = SHA256.new()
		hash.update(str.encode('utf-8'))
		return hash.digest()
	
	def registracion(self, login, address, user, msg = chr(203)):
		'''
		This function register user in game
		Arguments:
		login - login, which user want to have
		address - user's address e-mail
		user - object of class User
		msg - it is a message, which will be send to user after making 
			registration (it will be also send in case registration failed)
			In case everything is fine - number of answer is 201
		possible errors:
		202 - given string in address is not an e-mail address
		121 - database exception
		222 - there is an account on given e-mail address
		200 - login is taken
		'''
		if not e_mail.match(address):
			print("to nie jest adres e-mail")
			raise ServerError(202, msg)
		try:
			sql = sql_address(address)
		except:
			print("sql error - registration")
			raise ServerError(121, msg)
		if sql != []:
			print("Chyba juz masz konto :)")
			raise ServerError(222, msg)
		
		password = password_gen()
		hash = self._get_hash(password)
		try:
			sql_registration1(login, hash, address)
		except:
			raise ServerError(200, msg)
		self._send_mail(registration_mail, login, address, password)
		user.get_messanger().answer_user(201,msg)
	
	def login(self, login, password, user, msg = chr(201)):
		'''
		This function login user in game
		Arguments:
		login - user's login
		password - user's password
		user - object of class User
		msg - it is a message, which will be send to user after login
		(it will be also send in case login failed)
			In case everything is fine - number of answer is 201
		possible errors:
		121 - database exception
		222 - user is already login on another device
		200 - wrong login or/and password
		'''
		password = self._get_hash(password)
		try:
			sql = sql_check_login(login, password)
		except:
			print("sql error 1 - logowanie")
			raise ServerError(121, msg)
		if sql == []:
			print("zly login lub haslo")
			raise ServerError(200, msg)
		try:
			sql = sql_in_game(login)
		except:
			print("sql error 2 - logowanie")
			raise ServerError(121, msg)
		if sql!= []:
			print("juz jestes zalogowany")
			raise ServerError(222, msg)
		user.set_login(login)
		print("Logowanie")
		user.get_messanger().answer_user(201, msg)
		[game,player] = self.disscon(login)
		if (sql_pass_temp(login) != []):
			print("Ktos nowy sie pojawil  :)")
			user.get_messanger().answer_user(202)
		if(game != None):
			print("jest w grze")
			try:
				sql_add_to_game(login)
			except:
				print("sql error 3 - logowanie")
				raise ServerError(121, msg)
			#tutaj nalezy wyslac odpowiednie dane - spytac co jest potrzebne!!!!
			user.get_messanger().answer_user(7, game.id)
			user.set_game(game)
			user.set_player(player)
		else:
			try:
				sql_new_player(login)
			except:
				print("sql error 4 - logowanie")
				raise ServerError(121, msg)
			print("nie jest w grze")
			#czy tez trzeba cos wyslac??
	
	def create_game(self, user, teams, msg = chr(101)):
		'''
		This function create game
		Arguments:
		user - object of class User
		msg - it is a message, which will be send to user after creating game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			103 + {id of created game}
			
		possible errors:
		200 - there is no room for another game on server
		222 - user already created game or is in someone's game
		121 - database exception
		'''
		if len(self.get_games()) >= self.max_games:
			print("nie ma miejsca w gospodzie")
			raise ServerError(200, msg)
		if user.get_login()==None or user.get_game() != None:
			print("Nie mozesz stworzyc gry!")
			raise ServerError(222, msg)
			
		g = Game(user.get_login(), teams)
		id = tuple(pack("!I", g.id))
		try:
			sql_new_game(user.get_login(), g.id)
		except:
			print("sql error -create game")
			raise ServerError(222, msg)
		msg = ''
		for i in id:
			msg = msg + chr(i)
		user.get_messanger().answer_user(103, msg)
		self.rm_add_game(g, False)
		self.join_game(g.id, user)
		print("Stworzona gra")
	
	def start_game(self, user, msg = chr(103)):
		'''
		This function starts game
		Arguments:
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			10
		It is send to every player in this game
			
		possible errors:
		200 - there is not enought players or teams
		222 - game cannot be startes
		223 - some players ane not ready yet
		'''
		if user.get_game() == None or user.get_game().created_by != user.get_login():
			raise ServerError(222, msg)
		#to tylko tak teraz, potem bedzie prawdziwy plik!!!
		file = None
		try:
			user.get_game().load_points(file)
		except GameError as e:
			print(e.code)
			if e == 1:
				raise ServerError(222, msg)
			elif e == 2:
				raise ServerError(200, msg)
			else:
				#gracze jeszcze nie gotowi
				raise ServerError(223, msg)
		try:
			user.get_game().start()
		except GameError:
			raise ServerError(222, msg)
		send_to_everybody(user.get_game(), 10)
	
	def send_to_everybody(self, game, nr, msg = ''):
		#do wszystkich w grze
		for p in game.players:
			p.get_messanger().answer_user(nr, msg)
	
	def send_to_team(self, game, team, nr, msg = ''):
		for p in game.players:
			if p.get_team() == team:
				p.get_messanger().answer_user(nr, msg)

	def send_teams(self, user):
		msg = user.get_game().teams
		msg = msg[0] + chr(0) + msg[1] + chr(0)
		user.get_messanger().answer_user(110, msg)
				
	def list_of_players(self, game, user):
		msg = ''
		teams = game.teams
		for p in game.players:
			msg = msg + p.get_ID() + chr(0)
			if p.get_team() == teams[0]:
				msg = msg + chr(0)
			else:
				msg = msg + chr(1)
		user.get_messanger().answer_user(109, msg)
	
	def join_game(self, id, user, msg = chr(102)):
		'''
		This function append user to a game
		Arguments:
		id - id of game to which user want to join
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		200 - there is not enought players or teams
		121 - database exception
		222 - user is already in some other game or game has already started
		200 - player is already in this game or game does not exists
		'''
		bomb = None
		try:
			bomb = sql_bomb(user.get_login())
		except:
			print("sql error - join game")
			raise ServerError(121, msg)
		print(user.get_login())
		if user.get_game() != None:
			raise ServerError(222, msg)
		print(id)
		for g in self.get_games():
			if g.id == id:
				try:
					g.add_player(user.get_login(), user.get_messanger(), bomb[0][0], bomb[0][1], bomb[0][2], g.teams[0])
					try:
						sql_add_game(g.created_by, user.get_login())
					except:
						print("sql error - 2 - join game")
						raise ServerError(121, msg)
				except GameError as e:
					if e.code == 1:
						raise ServerError(222, msg)
						print("Nie mozesz teraz dolaczyc do gry")
					else:
						raise ServerError(200, msg)
				user.set_game(g)
				user.get_messanger().answer_user(201, msg)
				self.send_teams(user)
				self.list_of_players(g, user)
				self.send_to_everybody(user.get_game(), 102, chr(0)+user.get_login())
				return
		print("nie ma takiej gry")
		raise ServerError(200, msg)
	
	def end_game(self, user, team=3, msg = chr(4)):
		'''
		This function ends game
		Arguments:
		user - object of class User
		team - name of team or '3' when game is ended by player
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			4 + {name of winning team}
			
		possible errors:
		121 - database exception
		222 - user is not in the game or does not created this game
		'''
		#zlap wyjatek jak nie jest 
		g = user.get_game()
		if team == 3:
			if g == None or g.created_by != user.get_login():
				raise ServerError(222, msg)
				print("nie jestes w grze")
			try:
				sql_rmv_game(g.id)
			except:
				print("sql error 2 - end game")
				raise ServerError(121, msg)
		else:
			teams = user.get_game().teams
			if teams[0] == team:
				send_to_team(g, team, 4, chr(1))
				send_to_team(g, teams[1], 4, chr(1))
			else:
				send_to_team(g, team, 4, chr(1))
				send_to_team(g, teams[0], 4, chr(1))
		self.rm_add_game(g, True)
		print("Koniec gry")
	
	#jesli bedzie to szyfrowane to trzeba chyba bedzie zapamietac klucz, bo moze sie bedzie tym samym poslugiwac!!!!!
	#na razie jest to troche bez sensu napisane
	def connection_lost(self, user):
		if user.get_game() != None:
			try:
				sql_rmv_from_game(user.get_login())
			except:
				print("sql error - connection lost")
				#raise ServerError(121)
		elif user.get_login() != None:
			try:
				sql_logout(user.get_login())
			except:
				print("sql error - 2 - connection lost")
				#raise ServerError(121
		try:
			self.rm_add_user(user, True)
		except:
			pass

	def disscon(self, login):
		for g in self.get_games():
			for p in g.players:
				if login == p.get_ID():
					print("jestes w rozgrywce")
					return [g,p]
		return [None, None]
	
	def log_out(self, user, msg = chr(202)):
		'''
		This function log out user
		Arguments:
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		121 - database error
		222 - user was not logged in
		'''
		
		if user.get_login() == None:
			raise ServerError(222, msg)
			
		print("no to sie wyloguj.")
		try:
			sql_logout(user.get_login())
		except:
			print("sql error - log out")
			raise ServerError(121, msg)
		if user.get_player() != None:
			user.get_player().set_messanger(None)
		user.set_login(None)
		user.set_player(None)
		user.set_game(None)
		user.get_messanger().answer_user(201, msg)
	
	def new_user(self, conn):
		'''
		After connecting to the socket user gets the possibility to "converse" with server
		'''
		if len(self.get_users()) >= self.max_users:
			#to pewnie nie zadziala xD
			conn.send(222)
			return
		self.rm_add_user(User(self, conn, self.prot), False)
	
	def change_pass(self, login, old_pass, new_pass, user, msg = chr(204)):
		'''
		This function change user's password
		Arguments:
		login - user's login
		old_pass - old user's password
		new_pass - new user's password
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		121 - database exception
		200 - login or/and old password is wrong
		'''
		old_pass = self._get_hash(old_pass)
		new_pass = self._get_hash(new_pass)
		print("zmiana hasla")
		try:
			sql = sql_check_login(login,old_pass)
		except:
			print("sql error - change pass")
			raise ServerError(121, msg)
		if sql == []:
			print("zly login lub haslo")
			raise ServerError(200, msg)
		try:
			sql_update_pass(new_pass, login)
		except:
			print("sql error- 2 - change pass")
			raise ServerError(121, msg)
		sql_set_temp_pass1(login)
		user.get_messanger().answer_user(201, msg)

	def end(self, user):
		'''
		User end connection to the socket - object of class User is delated
		'''
		print("do zobaczenia")
		user.geisha.connection = False
		self.rm_add_user(user)
		
	def new_pass_login(self, address, user, msg = chr(205)):
		'''
		This function send on users e-mail new password and his login
		Arguments:
		address - address e-mail of user
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		200 - address e-mail does not exists in database
		121 - database exception
		'''
		try:
			sql = sql_address(address)
		except:
			print("sql error - new pass log")
			raise ServerError(121, msg)
		if  sql == []:
			print("nie ma takiego adresu w bazie")
			raise ServerError(200, msg)
	
		login = None
		try:
			login = sql_find_login(address)
		except:
			print("sql error -2 new pass log")
			raise ServerError(121, msg)
		password = password_gen()
		p = self._get_hash(password)
		try:
			sql_update_pass(p, login[0][0])
		except:
			print("sql error 3 new pass log")
			raise ServerError(121, msg)
		adres = address
		self._send_mail(change_pass_mail, login[0][0], adres, password)
		sql_set_temp_pass2(login[0][0])
		user.get_messanger().answer_user(201, msg)
	
	def location(self, position, user, msg = chr(1)):
		'''
		This function check current location of the user
		Arguments:
		loc - location of the user
		user - object of class User
		msg - it is a message send only when something went wrong
			
		possible errors:
		222 - user is not in game or game has not started yet
		'''
		if user.get_game() == None or user.get_game.phase < 3:
			raise ServerError(222, msg)
		user.get_player().set_position(position)
		bombs, point = user.get_game().update_player(user.get_player())
		
		#zapytac jak rozroznic, czy bomba wybucha wlasnie, co przeslac w point + przeslanie komunikatu do osoby, ktora
		# postawila bombe, ze wybuchla
		for bomb in bombs:
			user.get_messanger().answer_user(1, msg)
		if point != None:
			#przeslanie point do Marty
			user.get_messanger().answer_user(201, msg)
		
	def set_team_names(self, team_1, team_2, user, msg=chr(105)):
		'''
		This function set names of teams in game
		Arguments:
		team_1, team_2 - names of teams
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		200 - it is not possible to add new team in this moment
		222 - user is not in game, it is not his game or team already exists
		'''
		if user.get_login == None or user.get_game() == None or user.get_game().created_by != user.get_login():
			raise ServerError(222, msg)
		try:
			user.get_game().add_team(team_1)
			user.get_game().add_team(team_2)
		except GameError as e:
			if e.code < 3:
				raise ServerError(222, msg)
			else:
				raise ServerError(200, msg)
			print("nazwy druzyn juz zostaly ustalone czy cos")
		user.get_messanger().answer_user(201, msg)
		
	#TRZEBA JESZCZE SPRAWDZIC, CZY GRA SIE NIE ROZPOCZELA (chyba, ze to zostalo zostawione specjalnie -> szpiegowanie???)
	def change_team(self, team, user, msg=chr(104)):
		'''
		This function set team for user
		Arguments:
		team - name of team to which user want to join
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		222 - team does not exists or user is not in game
		200 - team cannot be change
		'''
		# w jakiej formie przyjdzie i jak jest przechowywane !!!
		if user.get_login() == None or user.get_game == None:
			raise ServerError(222, msg)
		try:
			user.get_game().assign(user.get_player(), user.get_game().teams[team])
		except GameError:
			raise ServerError(200, msg)
		user.get_messanger().answer_user(201, msg)
		msg = chr(team)+user.get_login()
		send_to_everybody(user.get_game(), 104, msg)
	
	def solution_puzzle(self, solution, user, msg=chr(5)):
		'''
		When user think he has the solution to the given puzzle
		Arguments:
		solution - true or false (1 or 0) (((or something)))
			true - answer is good
			false - answer is wrong
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			end of the game - if it was last point on the route
			5 + clue (to another point)
			
		possible errors:
		200 - wrong answer
		222 - user is not in game or game has not started yet
		'''
		if user.get_game() == None or user.get_game().phase < 3:
			raise ServerError(222, msg)
		if solution == 0:
			raise ServerError(200, msg)
		point = None
		#co to jest point?
		clue = user.get_game().score_point(user.get_player(), point)
		if len(clue) == 1:
			end_game(user, user.get_player().get_team(), clue[0])
		else:
			send_to_team(user.get_game(), user.get_player().get_team(), clue[0], 5, clue[1])
	
	def bomb(self, bomb_to_place, user, msg=chr(2)):
		'''
		User want to place bomb
		Arguments:
		bomb_to_place - bomb which user want to place
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		200 - it is not possible to place bomb in this moment
		222 - user is not in game or game has not started yet
		'''
		print(user)
		if user.get_game() == None or user.get_game().phase < 3:
			raise ServerError(222, msg)
		try:
			user.get_game().place_bomb(user, bomb_to_place)
		except GameError as e:
			raise ServerError(200, msg)
		user.get_messanger().answer_user(201, msg)
			
	def disconnect_user(self, user, msg = chr(3)):
		'''
		User don't want to play in game any longer
		Arguments:
		user - object of class User
		msg - it is a message, which will be send to user after start of the game
		(it will be also send in case function has failed)
			
		In case everything is fine answer is:
			201
			
		possible errors:
		200 - user is not in the game
		121 - database exception
		'''
		if user.get_game() == None:
			raise ServerError(200, msg)
			print("nie jestes w grze")
		print("Odlaczenie gracza")
		try:
			sql_set_game_null(user.get_login())
		except:
			print("sql error - disconnect user from game")
			raise ServerError(121, msg)
		user.get_game().delete_player(user.get_player())
		user.set_game(None)
		user.get_messanger().answer_user(201, msg)
		
	def stop(self):
		self.s.close()

def ending(s):
	while True:
		try:
			time.sleep(100)
		except:
			print ("bye bye")
			s.stop()
			db.close()
			return
			
if __name__ == "__main__":
	ending(Server())