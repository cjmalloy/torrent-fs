package com.cjmalloy.torrentfs.server.remote.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;

@Path("/")
public class Api
{
    @GET @Path("/sayhi")
    @Produces(MediaType.APPLICATION_JSON)
    public String sayHi(@Context HttpServletRequest req)
    {
        String token = "hi";
        return "\"" + token + "\"";
    }

    @POST @Path("/print")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String printTorrent(InputStream bencode)
    {
		String s;
		try
		{
			s = printValue(BDecoder.bdecode(bencode));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new WebApplicationException(400);
		}
		return s;
    }

	private String printValue(BEValue value) throws InvalidBEncodingException
	{
		if (value.getValue() instanceof byte[])
		{
			return "\"" + value.getString("UTF-8") + "\"";
		}
		else if (value.getValue() instanceof Integer)
		{
			return ""+value.getInt();
		}
		else if (value.getValue() instanceof Long)
		{
			return ""+value.getLong();
		}
		else if (value.getValue() instanceof Number)
		{
			return ""+value.getNumber();
		}
		else if (value.getValue() instanceof Map)
		{
			return printMap(value.getMap());
		}
		else if (value.getValue() instanceof List)
		{
			return printList(value.getList());
		}
		return "{}";
	}

	private String printMap(Map<String, BEValue> value) throws InvalidBEncodingException
	{
		String s = "{";
		boolean started = false;
    	for (String k : value.keySet())
    	{
    		if (started) s += ",";
    		started = true;
    		s += "\"" + k + "\": ";
    		if (k.equals("pieces"))
    		{
    			s += "\"snip\"";
    		}
    		else
    		{
        		s += printValue(value.get(k));
    		}
    	}
    	return s + "}";
	}

	private String printList(List<BEValue> value) throws InvalidBEncodingException
	{
		String s = "[";
		boolean started = false;
    	for (BEValue v : value)
    	{
    		if (started) s += ",";
    		started = true;
    		s += printValue(v);
    	}
    	return s + "]";
	}
}
