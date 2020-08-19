package com.friendsuggester.lua;

public class LuaScript {

	public static final String ADD_FRIEND_SCRIPT = ""
			+ "\nlocal areAlreadyFriends = tonumber(redis.call('SISMEMBER', KEYS[3], ARGV[2]));"
			+ "\nif (areAlreadyFriends == 1)"
			+ "\nthen"
			+ "\n return -1"
			+ "\nend"
			+ "\nlocal isAlreadyRequested = tonumber(redis.call('SISMEMBER', KEYS[2], ARGV[1]));"
			+ "\nlocal isReversePending = tonumber(redis.call('SISMEMBER', KEYS[1], ARGV[2]));"
			+ "\nif (isReversePending ==1)"
			+ "\nthen" 
			+ "\n local removePending = redis.call('SREM', KEYS[1], ARGV[2]);"
			+ "\n local addAtoB = redis.call('SADD', KEYS[3], ARGV[2]);"
			+ "\n local addBtoA = redis.call('SADD', KEYS[4], ARGV[1]);"
			+ "\n return 1"
			+ "\nelseif(isAlreadyRequested == 1)"
			+ "\nthen" 
			+ "\n  return 0"
			+ "\nelse" 
			+ "\n  local addPendingtoB=redis.call('SADD', KEYS[2], ARGV[1]);"
			+ "\n   return 1"
			+ "\nend";
	
	public static final String CREATE_USER_SCRIPT = ""
			+ "\nlocal isUserPresent = tonumber(redis.call('HEXISTS', KEYS[1], ARGV[1]));"
			+ "\nif (isUserPresent == 1)"
			+ "\nthen" 
			+ "\n    return -1"
			+ "\nend"
			+ "\nlocal createUser = redis.call('HMSET', KEYS[1], unpack(ARGV));"
			+ "\nreturn 1";

	public static final String GET_SUGGESTED_FRIENDS = ""
			+ "\nlocal function table_invert(t)"
			+ "\n    local s={}"
			+ "\n    for k,v in ipairs(t) do"
			+ "\n        s[v]=k"
			+ "\n    end"
			+ "\n    return s"
			+ "\n end"
			+ "\nlocal function addIfReq(vals, user, reverseMap, suggestions)"
			+ "\n    local rFriend = string.sub( user, string.len( 'friends:' )+1)"
			+ "\n    for j=1, #vals do"
			+ "\n        local val = vals[j]"
			+ "\n        if(val ~= rFriend and reverseMap[val] == nil)"
			+ "\n        then"
			+ "\n            table.insert( suggestions, val)"
			+ "\n        end"
			+ "\n    end"
			+ "\nend"
			+ "\nlocal suggestions={}"
			+ "\nlocal user = KEYS[1]"
			+ "\nlocal directFriends = redis.call('SMEMBERS', user)"
			+ "\nlocal reverseMap = table_invert(directFriends)"
			+ "\nfor i=1, #directFriends do"
			+ "\n    local directKey = 'friends:' .. directFriends[i]"
			+ "\n    local mutualFriends = redis.call('SMEMBERS', directKey)"
			+ "\n    addIfReq(mutualFriends, user, reverseMap, suggestions)"
			+ "\nend"
			+ "\nfor i=1, #suggestions do"
			+ "\n    local mutualKey = 'friends:' .. suggestions[i]"
			+ "\n    local friends = redis.call('SMEMBERS', mutualKey)"
			+ "\n    addIfReq(friends, user, reverseMap, suggestions)"
			+ "\nend"
			+ "\nreturn suggestions";

}