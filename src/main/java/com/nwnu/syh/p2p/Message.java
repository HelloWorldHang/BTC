package com.nwnu.syh.p2p;

import java.io.Serializable;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-05-09 18:57
 */
public class Message implements Serializable {

    private int type;

    private String data;

    public Message(){

    }

    public Message(int type){
        this.type = type;
    }

    public Message(int type, String data) {
        this.type = type;
        this.data = data;
    }


    public int getType() {
        return this.type;
    }

    public String getData() {
        return this.data;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", data='" + data + '\'' +
                '}';
    }
}
