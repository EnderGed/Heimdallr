'''
uwaga!!!!!!!!!!!!!!!!
NIE TESTOWANE !!!!!!!!!!!!!!!
jutro bede robila poprawki.
'''

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
import gra
from gra_klasy import Player

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
sql_registration1 = db.prepare("INSERT INTO users VALUES ($1, $2, $3)")
sql_registration2 = db.prepare("INSERT INTO bombs(login) VALUES ($1)")
sql_bomb = db.prepare("SELECT bomb_limit, bombs_in_inventory, bomb_radius FROM bombs WHERE login LIKE $1")
sql_in_game = db.prepare("SELECT login FROM in_game WHERE login LIKE $1 AND connected = TRUE")
sql_add_to_game = db.prepare("UPDATE in_game SET connected = TRUE WHERE login LIKE $1")
sql_rmv_from_game = db.prepare("UPDATE in_game SET connected = FALSE WHERE login LIKE $1")
sql_logout = db.prepare("DELETE FROM in_game WHERE login LIKE $1")
sql_new_player = db.prepare("INSERT INTO in_game VALUES ($1)")
sql_add_game = db.prepare("UPDATE in_game SET game = $1 WHERE login LIKE $2")
sql_rmv_game = db.prepare("UPDATE in_game SET game = NULL WHERE login LIKE $1")
sql_new_game = db.prepare("INSERT INTO games VALUES($1, $2)")
sql_rmv_game = db.prepare("DELETE FROM games WHERE ID LIKE $1")

def password_gen(size=8, chars=string.ascii_uppercase + string.digits):
	return ''.join(random.choice(chars) for x in range(size))
		
class Doorkeeper(threading.Thread):
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
					return
		
class Server:
	
	def __init__(self):
		self.HOST = ''
		self.PORT = 4747
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
	
	'''
	this functioin send e-mail to user -> registration or forgot password
	'''
	def _send_mail(self, mail, login, adres, password):
		SMTP_server = smtplib.SMTP('smtp.gmail.com', 587)
		sender = 'heimdallrgame@gmail.com'
		SMTP_server.ehlo()
		SMTP_server.starttls()
		SMTP_server.login(sender, "projektgry")
		receivers = [adres]
		w = mail
		w = w.replace("To: ", "To: <" + receivers[0] + ">")
		w = w.replace("Login: ", "Login: " + login)
		w = w.replace("Haslo: ", "Haslo: " + password)
		SMTP_server.sendmail(sender, receivers, w)
	
	def get_users(self): return self._users
	
	def __first(self):
		try: 
			self.s = socket(AF_INET, SOCK_STREAM) 
			self.s.bind((self.HOST,self.PORT)) 
			self.s.listen(5)
		except OSError:
			return
	
	def rm_add_user(self, user, b):
		if b:
			self._users.remove(user)
		else:
			self._users.append(user)
	
	'''
	function create hash of given string
	
	to jest do wpisywania hasla w bazie danych
	'''
	def _get_hash(self, str):
		hash = SHA256.new()
		hash.update(str.encode('utf-8'))
		return hash.digest()
	
	'''
	errors: 200 -> login is already taken
			202 -> address e-mail is not good
			222 -> address is already taken
	'''
	def registracion(self, message, user):
		
		if not e_mail.match(message[1]):
			print("to nie jest adres e-mail")
			raise ServerError(202)
		if sql_address(message[1]) != []:
			print("Chyba juz masz konto :)")
			raise ServerError(222)
		
		password = password_gen()
		hash = self._get_hash(password)
		try:
			sql_registration1(message[0], hash, message[1])
			sql_registration2(message[0])
		except:
			raise ServerError(200)
		self._send_mail(registration_mail, message[0], message[1], password)
		user.get_messanger().answer_user(201)
	
	'''
	errors: 200 -> login or password was wrong
			222 -> user is logged in on another device
	'''
	def login(self, message, user):
		message[1] = self._get_hash(message[1])
		if sql_check_login(message[0], message[1]) == []:
			print("zly login lub haslo")
			raise ServerError(200)
		if sql_in_game(message[0]) != []:
			print("juz jestes zalogowany")
			raise ServerError(222)
		user.set_login(message[0])
		print("Logowanie")
		user.get_messanger().answer_user(201)
		[game,player] = self.disscon(message[0])
		if(game != None):
			print("jest w grze")
			sql_add_to_game(message[0])
			#tutaj nalezy wyslc odpowiednie dane - spytac co jest potrzebne!!!!
			user.get_messanger().answer_user(7, game.id)
			user.set_game(game)
			user.set_player(player)
		else:
			sql_new_player(message[0])
			print("nie jest w grze")
			#czy tez trzeba cos wyslac??
	
	'''
	user can create game
	errors: 200 -> there is no place in server for this game
			222 -> user is already in game
	'''
	def create_game(self, user):
		if len(self.get_games()) >= self.max_games:
			print("nie ma miejsca w gospodzie")
			raise ServerError(200)
		if user.get_game() != None:
			print("Juz masz gre!")
			raise ServerError(222)
			
		g = gra.Game(user.get_login())
		user.get_messanger().answer_user(103, g.id)
		sql_new_game(user.get_login(), g.id)
		self.rm_add_game(g, False)
		self.join_game(g.id, user)
		print("Stworzona gra")
	
	'''
	errors: 222 -> user is not allowed to start game
	'''
	def start_game(self, user):
		if user.get_game().created_by != user.get_login():
			raise ServerError(222)
		#to tylko tak teraz, potem bedzie prawdziwy plik!!!
		file = None
		try:
			user.get_game().load_points(file)
		except gra.GameError as e:
			print(e.code)
			print("nie wiem dlaczego")
			raise ServerError(231)
		user.get_game().start()
		send_to_everybody(10)
	
	def send_to_everybody(self, game, nr, msg = ''):
		#do wszystkich w grze
		for p in game.players:
			p.get_messanger().answer_user(nr, msg)
	
	def send_to_team(self, game, team, nr, msg = ''):
		for p in game.players:
			if p.get_team() == team:
				p.get_messanger().answer_user(nr, msg)

	def list_of_players(self, game, user):
		msg = ''
		for p in game.players:
			msg = msg + p.get_ID + ' '
		user.get_messanger().answer_user(9, msg)
	
	'''
	errors: 200 -> user can not join game -> game has already started
			222 -> user already in game
	'''
	def join_game(self, id, user):
		bomb = sql_bomb(user.get_login())
		print(user.get_login())
		if user.get_game() != None:
			raise ServerError(222)
		for g in self.get_games():
			if g.id == id:
				try:
					g.add_player(user.get_login(), user.get_messanger(), bomb[0][0], bomb[0][1], bomb[0][2])
					sql_add_game(g.created_by, user.get_login())
				except gra.GameError:
					print("juz dolaczyles do gry")
					raise ServerError(200)
				user.set_game(g)
				user.get_messanger().answer_user(201)
				return
				
		raise ServerError(200)
		print("nie ma takiej gry")
	
	'''
	errors: 222 -> user is not allowed to end game
	'''
	def end_game(self, user):
		if user.get_game().created_by != user.get_login():
			raise ServerError(222)
		g = user.get_game()
		self.send_to_everybody(g, 4, 'bo tak')
		for p in g.players:
			p.set_game(None)
			sql_rmv_from_game(user.get_login())
		sql_rmv_game(g.id)
		self.rm_add_game(g, True)
		print("Koniec gry")
	
	'''
	when connection is lost user is delated from users
	'''
	#jesli bedzie to szyfrowane to trzeba chyba bedzie zapamietac klucz, bo moze sie bedzie tym samym poslugiwac!!!!!
	#na razie jest to troche bez sensu napisane
	def connection_lost(self, user):
		if user.get_game() != None:
			sql_rmv_from_game(user.get_login())
		else:
			sql_logout(user.get_login())
		self.rm_add_user(user, True)
		print("dddd1111")

	def disscon(self, login):
		for g in self.get_games():
			for p in g.players:
				if login == p.get_ID():
					print("jestes w rozgrywce")
					return [g,p]
		return [None, None]
	
	def log_out(self, user):
		print("no to sie wyloguj.")
		sql_logout(user.get_login())
		if user.get_player() != None:
			user.get_player().set_messanger(None)
		user.set_login(None)
		user.set_player(None)
		user.set_game(None)
		user.get_messanger().answer_user(201)
	
	'''
	errors: 222 -> there is no room for new user
	'''
	def new_user(self, conn):
		if len(self.get_users()) >= self.max_users:
			#to pewnie nie zadziala xD
			conn.send(222)
			return
		user = User(self, conn, self.prot)
		self.get_users().append(user)
	
	'''
	errors: 200 -> old password is wrong
	'''
	def change_pass(self, message, user):
		print("zmiana hasla")
		if sql_check_login(message[0], message[1]) == []:
			print("zly login lub haslo")
			raise ServerError(200)
		sql_update_pass(message[2], message[0])
		user.get_messanger().answer_user(201)

	def end(self, user):
		print("do zobaczenia")
		user.geisha.connection = False
		print(len(self.get_users()))
		self.rm_user(user)
		print(len(self.get_users()))
		
	'''
	errors: 200 -> database doesn't have given e-mail address
	'''	
	def new_pass_login(self, message, user):
		if sql_address(message[0]) == []:
			print("nie ma takiego adresu w bazie")
			raise ServerError(200)
		login = sql_find_login(message[0])
		password = password_gen()
		p = self._get_hash(password)
		sql_update_pass(p, login[0][0])
		adres = message[0]
		self._send_mail(change_pass_mail, login[0][0], adres, password)
		user.get_messanger().answer_user(201)
	
	'''
	errors: 222 -> user is not in game or game does not started
	'''
	def location(self, message, user):
		if user.get_game() == None or user.get_game.phase <= 0:
			raise ServerError(222)
		user.get_player().set_position(message)
		cos = user.get_game().update_player(user.get_player())
		
		#cos z tym cosiem trzeba zrobic
		
	'''
	errors: 222 -> user is not aloved 
			200 -> names already given
	'''
	#jak sie pisze czy ma pozwolenie po angielsku!!!!!
	def set_team_names(self, message, user):
		if user.get_name() == None or user.get_game().created_by != user.get_login():
			raise ServerError(222)
		try:
			user.get_game().add_team(message[0])
			user.get_game().add_team(message[1])
		except gra.GameError:
			raise ServerError(200)
			print("nazwy druzyn juz zostaly ustalone")
		
	'''
	errors: 200 -> user is not in game or game does not have team
	'''
	#TRZEBA JESZCZE SPRAWDZIC, CZY GRA SIE NIE ROZPOCZELA (chyba, ze to zostalo zostawione specjalnie -> szpiegowanie???)
	def change_team(self, message, user):
		# w jakiej formie przyjdzie i jak jest przechowywane !!!
		try:
			user.get_game().assign(user.get_player(), message[0])
		except gra.GameError:
			raise ServerError(200)
	
	'''
	errors: 222 -> user is not in game or game is not ready
			200 -> user sent wrong answer
	'''
	#trzeba zrobic co sie stanie, jesli odpowiedz bedzie zla -> trzeba zrobic sprawdzenie, czy
	#odpowiedz jest poprawna
	def solution_puzzle(self, message, user):
		if user.get_game() == None or user.get_game().phase <= 1:
			raise ServerError(222)
		#jaka funkcje wywolac???
	
	'''
	errors: 200	-> user can't place bomb (don't have any)
			222 -> user is not in game or game is not ready
	'''
	def bomb(self, message, user):
		if user.get_game() == None or user.get_game().phase <= 1 or user.get_player:
			raise ServerError(222)
		try:
			if(user.get_game().place_bomb(user, message[0])==None):
				raise ServerError(200)
		except GameError as e:
			print("chyba sie nie uda")
	
	def game_message(self, player, nr, message=""):
		player.get_messanger().answer_user(nr, message)
		
	def disconnect_user(self, user):
		if user.get_game() == None:
			raise ServerError(200)
			print("nie jestes w grze")
		print("Odlaczenie gracza")
		sql_rmv_game(user.get_login())
		user.get_game().delete_player(user.get_player())
		user.set_game(None)
		user.get_messanger().answer_user(201)
			
if __name__ == "__main__":
	Server()