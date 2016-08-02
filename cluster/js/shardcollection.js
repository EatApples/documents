var skey=db.runCommand({ shardcollection:"#args0#", key:#args1#});
printjson(skey);