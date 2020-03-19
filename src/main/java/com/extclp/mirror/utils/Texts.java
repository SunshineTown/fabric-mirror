package com.extclp.mirror.utils;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class Texts {

    public static LiteralText of(String str){
        return new LiteralText(str);
    }

    public static LiteralText of(String str, Object... args){

        LiteralText text = new LiteralText("");
        String[] split = str.split("%s");
        for (int i = 0; i < split.length; i++) {
            if(i != 0 && i <= args.length){
                Object arg = args[i - 1];
                if(arg instanceof Text){
                    text.append((Text) arg);
                }else {
                    text.append(arg.toString());
                }
            }
            text.append(split[i]);
        }
        return text;
    }
}
