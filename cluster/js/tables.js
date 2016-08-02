var cursor = db.collections.find({})
while(cursor.hasNext())
{
      var temp = cursor.next();
      printjson(temp);  
}