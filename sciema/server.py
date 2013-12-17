from socket import *
from email.mime.text import MIMEText
import random
import codecs
#import time
import threading
import postgresql
import smtplib
import string
import re

from protocol import *
from user import *
from game import *

#takie tam pierdoly
db = postgresql.open("pq://postgres:agnieszka1@localhost/uzytkownicy")
adres_email = re.compile(r"[^@]+@[^@]+\.[^@]+")

myfile = open ("D:\\folder\\pwr\\projekt\\mail.txt", encoding="ascii", errors="ignore")	
registration_mail=myfile.read()
myfile.close()

myfile = open ("D:\\folder\\pwr\\projekt\\noweHaslo.txt", encoding="ascii", errors="ignore")	
change_pass_mail=myfile.read()
myfile.close()

sql_login = db.prepare("SELECT * FROM users WHERE login LIKE $1")
sql_address = db.prepare("SELECT * FROM users WHERE adres LIKE $1")
sql_check_login = db.prepare("SELECT * FROM users WHERE login LIKE $1 AND haslo LIKE $2")
sql_update_pass = db.prepare("UPDATE users SET haslo = $1 WHERE login LIKE $2")
sql_find_login = db.prepare("SELECT login FROM users WHERE adres LIKE $1")
	
def haslo_gen(size=8, chars=string.ascii_uppercase + string.digits):
	return ''.join(random.choice(chars) for x in range(size))

def send_mail(mail, message, haslo):
	SMTP_server = smtplib.SMTP('smtp.gmail.com', 587)
	sender = 'heimdallrgame@gmail.com'
	SMTP_server.ehlo()
	SMTP_server.starttls()
	SMTP_server.login(sender, "projektgry")
	receivers = [message[1]]
	login = message[0]
	w = mail
	w = w.replace("To: ", "To: <" + receivers[0] + ">")
	w = w.replace("Login: ", "Login: " + login)
	w = w.replace("Haslo: ", "Haslo: " + haslo)
	SMTP_server.sendmail(sender, receivers, w)		
		
class Doorkeeper(threading.Thread):
	def __init__(self, server):
		threading.Thread.__init__(self)
		self.server = server
		
	def run(self):
		while True:
			if len(self.server.users) < 3:
				conn, addr = self.server.s.accept()
				self.server.new_user(conn)
		
class Server:
	
	def __init__(self):
		self.HOST = ''
		self.PORT = 9999
		self.users = []
		self.games = []
		self.s = None 
		self.__first()
		self.prot = Protocol(self)
		self.lokaj = Doorkeeper(self)
		self.lokaj.start()
	
	def __first(self):
		try: 
			self.s = socket(AF_INET, SOCK_STREAM) 
			self.s.bind((self.HOST,self.PORT)) 
			self.s.listen(5)
		except: 
			if self.s: 
				self.s.close() 
			print("Nie udalo sie otworzyc servera: ")
			return
	
	def create_game(self, user):
		#to tylko tak do testowania wlasciwie
		if len(self.games) >= 1:
			print("nie ma miejsca w gospodzie")
			user.get_messanger().answer_user("nie_mozna_stworzyc_gry")
			return
		if user.get_game() != None:
			print("Juz stworzyles gre!")
			user.get_messanger().answer_user("Nie mozesz")
			return
			
		g = Game(self, user)
		self.games.append(g)
		user.set_game(g)
		print("Do stworzenia games")
		user.get_messanger().answer_user("OK")
	
	def start_game(self, user):
		#tylko osoba, ktora stworzyla gre moze ja rozpoczac!!!
		user.get_game().start_rozgrywki()
		user.get_messanger().answer_user("OK")
		
	def join_game(self, message, user):
		if self.games == []:
			user.get_messanger().answer_user("Nie ma gier")
			return
		for g in self.games:
			if g.id == message[0]:
				if g.add_user(user):
					user.set_game(g)
					user.get_messanger().answer_user("dolaczono")
				else:
					user.get_messanger().answer_user("max_liczba_graczy")
				return
		print("nie ma takiej games")
		user.get_messanget().answer_user("sorki_nie_ma_takiej_games")
		print("Dolaczenie do games")
		
	def end_game(self, id):
		print("nie chcemy juz grac")
		for g in self.games:
			if g.id == id:
				g.end_game()
				self.games.remove(g)
				return
		print("Koniec games")
		
	def login(self, message, user):
		if sql_check_login(message[0], message[1]) == []:
			print("zly login lub haslo")
			user.get_messanger().answer_user("zle_dane")
			return
		user.set_login(message[0])
		print("Logowanie")
		user.get_messanger().answer_user("ok_dobrze_jest")
	
	def log_out(self, user):
		print("no to sie wyloguj.")
		user.set_login(None)
		user.get_messanger().answer_user("ok")
	
	def registracion(self, message, user):
		if not adres_email.match(message[1]):
			print("to nie jest adres e-mail")
			user.get_messanger().answer_user("zly_adres")
			return
		if sql_login(message[0])!= []:
			print("zajete")
			user.get_messanger().answer_user("login_zajety")
			return
		if sql_address(message[1]) != []:
			print("Chyba juz masz konto :)")
			user.get_messanger().answer_user("adres_ma_juz_konto")
			return
		
		haslo = haslo_gen()
		db.execute("INSERT INTO users values ('" + message[0] + "', '" + haslo + "', '" + message[1] + "')")
		
		send_mail(registration_mail, message, haslo)
		
		user.get_messanger().answer_user("OK")
		
	def disconnect_user(self, user):
		if user.get_game() == None:
			user.get_messanger().answer_user("Nie jestes w grze")
			return
		print("Odlaczenie gracza")
		user.get_game().disconnect_user(user)
		user.set_game(None)
		
	def location(self, message, user):
		if user.get_game() == None:
			user.get_messanger().answer_user("Nie jestes w grze")
			return
		user.get_player().set_position(message)
		user.get_messanger().answer_user("OK")
		
	def new_user(self, conn):
		user = User(self, conn, self.prot)
		self.users.append(user)
	
	def change_pass(self, message, user):
		print("zmiana hasla")
		if sql_check_login(message[0], message[1]) == []:
			print("zly login lub haslo")
			user.get_messanger().answer_user("zle_dane")
			return
		sql_update_pass(message[2], message[0])
		user.get_messanger().answer_user("haslo zmienione")

	def end(self, user):
		print("do zobaczenia")
		user.geisha.connection = False
		print(len(self.users))
		self.users.remove(user)
		print(len(self.users))
		
	def new_pass_login(self, message, user):
		if sql_address(message[0]) == []:
			print("nie ma takiego adresu w bazie")
			user.get_messanger().answer_user("nie_mam_w_bazie_takiego_adresu")
			return
		login = sql_find_login(message[0])
		haslo = haslo_gen()
		message = login + message
		print(message)
		send_mail(change_pass_mail, message, haslo)
		user.get_messanger().answer_user("dane_wyslane")
	
	def solution_puzzle(self, message, user):
		if user.get_game() == None:
			user.get_messanger().answer_user("Nie jestes w grze")
			return
		user.get_game().solution_puzzle(message[0], user)
	
	def bomb(self, message, user):
		if user.get_game() == None:
			user.get_messanger().answer_user("Nie jestes w grze")
			return
		user.get_game().bomb(user, message[0])
		
if __name__ == "__main__":
	Server()