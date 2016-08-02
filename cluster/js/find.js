var cursor = db.#args0#.find(#args1#,#args2#);
while(cursor.hasNext())
{
      var x = cursor.next();
      printjson(x);  
}