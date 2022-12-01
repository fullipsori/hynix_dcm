package com.skhynix.neesp;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Demo implements java.io.Serializable
{
    public int a;
    public String b;
  
    public Map<String, ArrayList<String>> mapSWNODEINFO = new HashMap<>();
    // Default constructor
    public Demo(int a, String b)
    {	
        this.a = a;
        this.b = b;        
        System.out.println(String.format("ver.2022.11.13 - hashMap Version. new creation [%d][%s]", a, b));
    }
}