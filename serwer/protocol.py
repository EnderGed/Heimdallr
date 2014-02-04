from struct import unpack

class ServerError(Exception):
	def __init__(self, code, msg=''):
		self.code = code
		self.msg = msg
		
class Protocol:
	def __init__(self, server):
		self.server = server
	
	def messages(self, w, from_user):
		order = w[0]
		print(order)
		try:
			if order == 1:
				l1 = None
				l2 = None
				try:
					l1 = unpack('d', w[1:9:1])[0]
					l2 = unpack('d', w[9:17:1])[0]
				except:
					from_user.get_messanger().answer_user(255)
				self.server.location([l1,l2], from_user)
			elif order == 2:
				l1 = None
				l2 = None
				try:
					l1 = unpack('d', w[1:9:1])[0]
					l2 = unpack('d', w[9:17:1])[0]
				except:
					from_user.get_messanger().answer_user(255)
				self.server.bomb([l1,l2], from_user)
			elif order == 3:
				self.server.disconnect_user(from_user)
			elif order == 4:
				self.server.end_game(from_user)
			elif order == 5:
				self.server.solution_puzzle(int(w[1]), from_user)
			elif order == 101:
				message = ''
				for i in w[1::]:
					message = message + chr(i)
				message = message.split(chr(0))
				self.server.create_game(from_user, [message[0], message[1]])
			elif order == 102:
				game_id = None
				try:
					game_id = unpack('>I', w[1:5:1])[0]
					print(game_id)
				except:
					from_user.get_messanger().answer_user(255)
				self.server.join_game(game_id, from_user)
			elif order == 103:
				self.server.start_game(from_user)
			elif order == 104:
				self.server.change_team(int(w[1]), from_user)
			elif order == 105:
				self.server.out_of_lobby(from_user)
			elif order == 201:
				message = ''
				for i in w[1::]:
					message = message + chr(i)
					print(i)
				message = message.split(chr(0))
				print(message)
				self.server.login(message[0], message[1], from_user)
			elif order == 202:
				self.server.log_out(from_user)
			elif order == 203:
				message = ''
				for i in w[1::]:
					message = message + chr(i)
				message = message.split(chr(0))
				print(message[0] + "  " + message[1])
				self.server.registracion(message[0], message[1], from_user)
			elif order == 204:	
				message = ''
				for i in w[1::]:
					message = message + chr(i)
				message = message.split(chr(0))
				self.server.change_pass(message[0], message[1], from_user)
			elif order == 205:
				message = ''
				for i in w[1::]:
					message = message + chr(i)
				message = message.split(chr(0))
				self.server.new_pass_login(message[0], from_user)
			else:
				print("NIE MA TAKIEGO POLECENIA")
				from_user.get_messanger().answer_user(255, chr(order))
		except IndexError:
			from_user.get_messanger().answer_user(255, chr(order))
		except ServerError as e:
			from_user.get_messanger().answer_user(e.code, e.msg)