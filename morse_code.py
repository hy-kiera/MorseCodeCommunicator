import socket
import fcntl
import struct
import asyncio
import websockets 

MORSE_CODE_DICT = {"A":"-", "B":"=...", "C":"-.-", "D":"-..", "E":".",
        "F":"..-.", "G":"--.", "H":"....", "I":"..", "J":".---", "K":"-.-",
        "L":"-..", "M":"--", "N":"-.", "O":"---", "P":".--.", "Q":"--.-",
        "R":".-.", "S":"...", "T":"-", "U":"..-", "V":"...-", "W":".--",
        "X":"-..-", "Y":"-.--", "Z":"--..", "1":"----", "2":"..---", "3":"...--",
        "4":"....-", "5":".....", "6":"-....", "7":"--...", "8":"---..", "9":"----.",
        "0":"-----", ",":"--..--", ".":".-.-.-", "?":"..--..", "/":"-..-.", "-":"....-",
        "(":"-.--.", ")":"-.--.-"}

# Websocket connection with ESP32
# def server():
#     ip_addr = get_ipaddress("wlan0")
#     print("Server start : " + ip_addr)

#     port = 80
#     asyncio.get_event_loop().run_until_complete(websockets.serve(echo, ip_addr, port))
#     asyncio.get_event_loop().run_forever()

# def get_ipaddress(network):
#     s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
#     return socket.inet_ntoa(fcntl.ioctl(s.fileno(),
#         0x8915, # SIOCGIFADDR
#         struct.pack("256s", network[:15].encode("utf-8"))
#         )[20:24])

# async def echo(websocket, path):
#     async for message in websocket:
#         print(message)
        # print(decrypt(message))
#         await websocket.send(message)
        # await websocket.send(encrypt(message.upper))
        # await websocket.recv()
# def client():
#    ws = websocket.WebSocket()
#    ws.connect()

#    i = 0
#    num_of_msg = 200

#    while i < num_of_msg:
#        ws.send("message num: " + str(i))
#        result = ws.recv()
#        print(result)
#        i = i + 1
#        time.sleep(1)

#    ws.close()


# Function to encrypt the string
# according to the morse code chart
def encrypt(message):
    cipher = ""
    for letter in message:
        if letter != " ":
            
            # Looks up the dictionary and adds the
            # correspponding morse code
            # along with a space to separate
            # morse codes for different characters
            cipher += MORSE_CODE_DICT[letter] + " "
        else:
            # 1 space indicates different characters
            # and 2 indicates different words
            cipher += " "
            
    return cipher

# Function to decrpyt the string
# from morse to english
def decrypt(message):
    
    # extra space added at the end to access the
    # last morse code
    message += " "
    
    decipher = ""
    citext = ""
    for letter in message:
        
        # checks for space
        if (letter != " "):
            # counter to keep track of space
            i = 0
            
            # storing morse code of a single character
            citext += letter
            
        # in case of space
        else:
            # if i = 1 that indicates a new character
            i += 1
            
            # if i = 2 that indicates a new word
            if i == 2:
                
                #  adding space to separate words
                decipher += " "
            else:
                
                # accessing the keys using their values
                # (reverse of encryption)
                decipher += list(MORSE_CODE_DICT.keys())[list(MORSE_CODE_DICT.values()).index(citext)]
                citext = ""
                
    return decipher

# Hard-coded driver function to run the program
def main():
    server()

    message = "Hello World"
    result = encrypt(message.upper())
    print(result)
    
    message = "... --- ..."
    result = decrypt(message)
    print(result)
    
# Executes the main function
if __name__ == "__main__":
    main()
