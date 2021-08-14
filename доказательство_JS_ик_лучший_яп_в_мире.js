function getRandomChar() {
  let chars = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890';
  return chars[ Math.floor(Math.random() *  (chars.length-1)) ];
}

function createString(length) {
  let half = parseInt( length / 2 );
  let result = '';

  for(let i=0; i<half; i++ ) {
    result += getRandomChar();
  }

  result += '{REPLACE_ME}';

  for(let i=0; i<half; i++ ) {
    result += getRandomChar();
  }

  return result;
}

function test(strLength, count) {
  let startTime = Date.now();
  console.log( `str_length=${strLength}, test_count=${count}, start_time= ${startTime}`);

  let soutseString = createString(strLength);
  let rep = new RegExp('{REPLACE_ME}', 'g');

  for(i=0; i<count; i++) {
    let res = soutseString.replace(rep, '~TEST~');
  }

  let worktime = Date.now() - startTime;
  console.log( `RESULT=${worktime / 1000}`);
}

console.log( ' ~TEST~' );

test(1000, 100000000);
test(10000, 10000000);
test(100000, 1000000);
