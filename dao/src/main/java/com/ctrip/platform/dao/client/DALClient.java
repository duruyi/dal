package com.ctrip.platform.dao.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ctrip.platform.dao.enums.ActionTypeEnum;
import com.ctrip.platform.dao.enums.FlagsEnum;
import com.ctrip.platform.dao.enums.MessageTypeEnum;
import com.ctrip.platform.dao.enums.ResultTypeEnum;
import com.ctrip.platform.dao.param.Parameter;
import com.ctrip.platform.dao.request.DefaultRequest;
import com.ctrip.platform.dao.request.RequestMessage;
import com.ctrip.platform.dao.response.DefaultResponse;
import com.ctrip.platform.dao.utils.Consts;
import com.ctrip.platform.dao.utils.DAOResultSet;

public class DALClient {
	Socket requestSocket;
	DataOutputStream out;
	DataInputStream in;

	public ResultSet fetch(String tnxCtxt, int flag, String statement,
			Parameter... params) throws Exception {

		RequestMessage msg = new RequestMessage();

		msg.setMessageType(MessageTypeEnum.SQL);
		msg.setActionType(ActionTypeEnum.SELECT);
		msg.setUseCache(false);

		msg.setSql(statement);

		msg.setArgs(new ArrayList<Parameter>(Arrays.asList(params)));

		msg.setFlags(FlagsEnum.TEST.getIntVal());

		DefaultRequest request = new DefaultRequest();

		request.setTaskid(UUID.randomUUID());

		request.setDbName(Consts.databaseName);

		request.setCredential(Consts.credential);

		request.setMessage(msg);
		
		DAOResultSet rs = new DAOResultSet(this.<List<List<Parameter>>>run(request));
		
		return rs;

//		return null;
	}
	
	public ResultSet fetchBySp(String tnxCtxt, int flag, String sp,
			Parameter... params) throws Exception {
		
		RequestMessage msg = new RequestMessage();

		msg.setMessageType(MessageTypeEnum.SP);
		msg.setActionType(ActionTypeEnum.SELECT);
		msg.setUseCache(false);
		
		msg.setSpName(sp);
		
		msg.setArgs(new ArrayList<Parameter>(Arrays.asList(params)));

		msg.setFlags(FlagsEnum.TEST.getIntVal());

		DefaultRequest request = new DefaultRequest();

		request.setTaskid(UUID.randomUUID());

		request.setDbName(Consts.databaseName);

		request.setCredential(Consts.credential);

		request.setMessage(msg);
		
		DAOResultSet rs = new DAOResultSet(this.<List<List<Parameter>>>run(request));
		
		return rs;
	}

	public int execute(String tnxCtxt, String statement, int flag,
			Parameter... params) throws Exception {

		RequestMessage msg = new RequestMessage();

		msg.setMessageType(MessageTypeEnum.SQL);
		msg.setActionType(ActionTypeEnum.DELETE);
		msg.setUseCache(false);

		msg.setSql(statement);
		
		msg.setArgs(new ArrayList<Parameter>(Arrays.asList(params)));

		msg.setFlags(FlagsEnum.TEST.getIntVal());

		DefaultRequest request = new DefaultRequest();

		request.setTaskid(UUID.randomUUID());

		request.setDbName(Consts.databaseName);

		request.setCredential(Consts.credential);

		request.setMessage(msg);

		return this.<Integer>run(request);

//		return 0;
	}
	
	public int executeSp(String tnxCtxt, String sp, int flag,
			Parameter... params) throws Exception {
		
		RequestMessage msg = new RequestMessage();

		msg.setMessageType(MessageTypeEnum.SP);
		msg.setActionType(ActionTypeEnum.DELETE);
		msg.setUseCache(false);

		msg.setSpName(sp);
		
		msg.setArgs(new ArrayList<Parameter>(Arrays.asList(params)));

		msg.setFlags(FlagsEnum.TEST.getIntVal());

		DefaultRequest request = new DefaultRequest();

		request.setTaskid(UUID.randomUUID());

		request.setDbName(Consts.databaseName);

		request.setCredential(Consts.credential);

		request.setMessage(msg);

		return this.<Integer>run(request);

	}

	<T> T run(DefaultRequest request) {
		try {
			// 1. creating a socket to connect to the server
			requestSocket = new Socket("localhost", 9000);

			// 2. get Input and Output streams
			out = new DataOutputStream(requestSocket.getOutputStream());

			in = new DataInputStream(requestSocket.getInputStream());
			// 3: Communicating with the server

			byte[] payload = request.packToByteArray();
			
			out.writeInt(payload.length + 2);

			out.writeShort(Consts.protocolVersion);

			out.write(payload, 0, payload.length);

			out.flush();
			
			int leftLength = in.readInt();

			short protocolVersion = in.readShort();
			
			byte[] leftData = new byte[leftLength - 2];
			
			in.read(leftData, 0, leftLength - 2);
			
			DefaultResponse response = DefaultResponse.unpack(leftData);
			
			if(response.getResultType() == ResultTypeEnum.CUD){
				return (T) new Integer(response.getAffectRowCount());
			}else{
				return (T) response.getResultSet();
			}

//			if (response.getResultType() == ResultTypeEnum.CUD) {
//				//System.out.println("affect row count: "+response.getAffectRowCount());
//				return (T) new Integer(response.getAffectRowCount());
//			}else{
//				
//				List<List<Parameter>> resultSet = new ArrayList<List<Parameter>>();
//				
//				for(int i=0;i< response.getChunkCount();i++){
//					int currentChunkSize = in.readInt();
//					
//					byte[] currentChunkData = new byte[currentChunkSize];
//					in.read(currentChunkData, 0, currentChunkSize);
//					
//					resultSet.addAll(DefaultResponse.unpackChunk(currentChunkData));
//				}
//				
//				return (T) resultSet;
//				
//			}

		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				out.close();
				in.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		
		return null;
	}

	public static void main(String[] args) {
		new DALClient();
	}

}
