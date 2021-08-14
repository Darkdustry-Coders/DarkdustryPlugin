from random import randrange
import time

def getRandomChar():
  chars = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890'
  return chars[ randrange(0, len(chars) - 1) ]

def createString(length):
  result = ''
  half = (int)( length / 2 )

  i = 0
  while i < half:
    result += getRandomChar()
    i += 1

  result += '{REPLACE_ME}'

  i = 0
  while i < half:
    result += getRandomChar()
    i += 1

  return result

def test(strLength, count):
  startTime = time.time()
  print( "str_length=" + str(strLength) + ", test_count=" + str(count) + ", start_time=" + str(startTime) )

  soutseString = createString(strLength)

  i = 0
  while i < count:
    i += 1
    res = soutseString.replace("{REPLACE_ME}", "~TEST~")
  
  worktime = time.time() - startTime
  print( "RESULT=" + str(worktime) )

test(1000, 100000000)
test(10000, 10000000)
test(100000, 1000000)