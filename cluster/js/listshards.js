var ls=db.runCommand({ listshards:1 });
printjson(ls);