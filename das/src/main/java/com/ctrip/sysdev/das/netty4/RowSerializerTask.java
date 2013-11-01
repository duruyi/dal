package com.ctrip.sysdev.das.netty4;

import static org.msgpack.template.Templates.tList;
import static org.msgpack.template.Templates.TValue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.msgpack.packer.BufferPacker;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.sysdev.das.domain.Response;

public class RowSerializerTask implements Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(RowSerializerTask.class);
	private ChannelHandlerContext ctx;
	private List<List<Value>> rows;
	private Response response;
	
	public RowSerializerTask(ChannelHandlerContext ctx, List<List<Value>> rows, Response response) {
		this.ctx = ctx;
		this.rows = rows;
		this.response = response;
	}
	
	@Override
	public void run() {
		try {
			byte[] bytes = serialize(rows);
			ByteBuf bf = ctx.alloc().buffer();

			bf.writeInt(bytes.length + 1);
//			Thread.sleep(1);
			bf.writeByte(response == null? 0 : 1);
			bf.writeBytes(bytes);
			ChannelFuture wf = ctx.writeAndFlush(bf);
			
			// Only the last one need to pass response
			if (response == null)
				return;

			wf.addListener(new ResponseWriteCompleteListener(response));
		} catch (Exception e) {
			logger.error("Error", e);
		}
		rows.clear();
		rows = null;

	}
	

	private byte[] serialize(List<List<Value>> rows) throws Exception {
		BufferPacker packer = ctx.channel().attr(ResponseSerializer.MESSAGE_PACK_KEY).get();
		packer.clear();
//		MessagePack msgpack = new MessagePack();
//		BufferPacker packer = msgpack.createBufferPacker();
		
		//Template<List<List<Value>>> tListOfList = tList(tList(TValue));
		Template<List<Value>> tList = tList(TValue);
		
//		packer.write(tListOfList);
		
		packer.writeArrayBegin(rows.size());
		for (List<Value> row : rows) {
			packer.write(row);
//			packer.writeArrayBegin(row.length);
//			for(Value column: row) {
//				packer.write(column);
//			}
//			packer.writeArrayEnd();
		}
		packer.writeArrayEnd();
		return packer.toByteArray();
	}

}
