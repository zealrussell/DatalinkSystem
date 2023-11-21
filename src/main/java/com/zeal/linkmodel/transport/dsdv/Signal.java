package com.zeal.linkmodel.transport.dsdv;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 19:28
 */
public class Signal {
    private  int needUpdate=30;
    private static final Signal instance = new Signal();

    public Signal () {
        set();
    }
    public void set () {
        needUpdate+=1;
    }
    public void sub () {
        needUpdate-=1;
    }
    public int get (){
        return needUpdate;
    }
    public static Signal getInstance(){
        if(instance!=null)
            return instance;
        return new Signal();
    }
}
