/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;


public final class Route
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
	public static int INF=0;
	static double[][] map=new double[601][601]; //存放边和点的权值
	static String[][] edge=new String[601][601]; //存放边的编号	
	static double[][] inforC=new double[601][601]; //信息量
	static int nodenum=0;
	static int edgenum=0;
	static int original=-1;
	static double infmax=20; //信息量的最大值
	static int antnum=20;
	static String[] isvistend=new String[antnum]; //每一只蚂蚁已经访问过的结点，这里定义10只蚂蚁
	static String[] edgevisited=new String[antnum];
	static boolean[] isexit=new boolean[601];
	static double bata=0.9;  //怎么设置
	static double wconst=0;
	static int[] midpoint=new int[601]; 
	static int[] midindex;
	static int NC=400;
	static double arfa=0.9;
	
//	public static boolean [] isvisited=new boolean[601];
	public static boolean [] needvisited=new boolean[601];
//	public static NodeIfor [] nodeinfor=new NodeIfor[601];
	public static int min_dis=500;
	public static List<String> min_path;
	public static int end;
	public static int start;
	public static int mid_num=0;
//	public static Map<String,List<GNode>> graphy=new HashMap();
	public static String resultFilePath="";
	public static long start_time = System.currentTimeMillis();
    public static String searchRoute(String graphContent, String condition)
    {
    	//resultFilePath=FilePath;
    	//将条件读取出来，并保存到相应的变量中
    	String [] split_condition=condition.split(",");
    	start=Integer.parseInt(split_condition[0]);
    	end=Integer.parseInt(split_condition[1]);
    	//读取中间要经过的点
    	//Map <String,Boolean> map_mid_point =new HashMap();   	
    	String [] mid_point=split_condition[2].trim().split("\\|");
    	mid_num=mid_point.length;
    	midindex=new int[mid_num];
    	for(int i=0;i<mid_num;i++)
    	{
    		midpoint[Integer.parseInt(mid_point[i])]=1;
    		midindex[i]=Integer.parseInt(mid_point[i]);
    	}
    	
    	//得出图的算法
    	String[] str_graphy=graphContent.split("\\n");

    	//初始化
    	for(int i=0;i<601;i++)
    		for(int j=0;j<601;j++)
    			map[i][j]=INF;
//    	for(int i=0;i<601;i++)
//    	{
//    		edge[i]=new Edge();
//    	}
    	
    	for(int i=0;i<str_graphy.length;i++)
    	{
    		String[] row=str_graphy[i].split(",");
    		int u,v,value;
    		u=Integer.parseInt(row[1]);
    		v=Integer.parseInt(row[2]);
    		value=Integer.parseInt(row[3]);
    		edge[u][v]=row[0];//保存边的编号
    		if(midpoint[v]==1)
    		{
    			map[u][v]=value;
    			inforC[u][v]=infmax*(nodenum/(mid_num*1.0));  //中间必过的的信息量要增大
    		} 			
    		else
    		{
    			map[u][v]=10*value;
    			inforC[u][v]=infmax;
    		}
    			
//    		else
//    			map[Integer.parseInt(row[1])][Integer.parseInt(row[2])]=Integer.parseInt(row[3]);
			if(!isexit[v])
			{
				nodenum++;
				isexit[v]=true;
			}
    		edgenum++;
    	}
    	wconst=nodenum;   
    	if(nodenum>250)
    	{
    		NC=800;
    	}
    	String result=ANT();
    	if(result.length()==0)
    		return "NA";
    	//result=result.replaceAll("\\|\\|", "\\|");
    	//result.replace(',', '|');
        return result;
    }
    
    public static String ANT()
    {
    	double infmin=infmax/(2.0*nodenum);
    	int iter=0; //迭代次数
    	String path=""; //记录全局最优解
    	double pathcost=99999999; //全局最优解的花费
    	boolean flag=true;  //是否已经收敛
    	while(iter<NC)
    	{
    		double[] cost=new double[antnum];
    		int minpath=-1; //表示最短路径编号，是哪只蚂蚁找到的。
    		double mincost=99999999;
    		for(int i=0;i<antnum;i++)
    		{
    			int midpointnum=0;
    			int vistpoint=start;
    			boolean haschild=true;
    			boolean isend=false;
    			isvistend[i]=""; //已经第i只蚂蚁访问过的点
    			edgevisited[i]="";
    			cost[i]=0; //路径上的花费
    			int[] midvisited=midpoint.clone();
    			while(!isend && haschild)
    			{
    				haschild=false;    				
    				double sumP=0;
    				double[] P=new double[300];
    				int [] index=new int[300];
    				int count=0; //表示可访问的子节点的个数
    				for(int j=0;j<nodenum;j++)
    				{    					
    					if((int)map[vistpoint][j]!=INF && isvistend[i].indexOf("|"+j+"|")<0)//表示点是否被访问
    					//if((int)map[vistpoint][j]!=INF && isvistend[i].indexOf("|"+edge[vistpoint][j]+"|")<0) //表示边是否被访问
    					{
    						if(j==end && midpointnum==mid_num)
    						{
    							haschild=true;
        						P[count]=inforC[vistpoint][j]*(1.0/map[vistpoint][j]);
        						index[count]=j;
        						sumP+=P[count];
        						count++;
    							isend=true;
	    						break;
    						}
    						else if(j==end && midpointnum!=mid_num)
    							continue;
    						haschild=true;
    						P[count]=inforC[vistpoint][j]*(1.0/map[vistpoint][j]);
    						index[count]=j;
    						sumP+=P[count];
    						count++;
    						    							    					
    					}					
    				}
    				
    				if(!haschild)
    				{
    					break;
    				}
    				//是否是终点
    				if(isend==true)
    				{
    					isvistend[i]+="|"+end+"|";//将点加进去
    					edgevisited[i]+=edge[vistpoint][end]; //将边加进去
						cost[i]+=map[vistpoint][end];  //这里先以路径最短为判断标准，后面要改
						break;
    				}
    				//使用轮盘赌算法选择要访问的结点
    				double randP=Math.random();
    				double temp=0;
    				for(int j=0;j<count;j++)
    				{
    					temp+=(P[j]/sumP);
    					if(temp>randP)
    					{
    						isvistend[i]+="|"+index[j]+"|";
    						edgevisited[i]+=edge[vistpoint][index[j]]+"|";
    						cost[i]+=map[vistpoint][index[j]];  //这里先以路径最短为判断标准，后面要改
    						if(midpoint[index[j]]==1)
    						{
    							midpointnum++;
    							midvisited[index[j]]=0;
    						}
    						//inforC[vistpoint][index[j]]=arfa*inforC[vistpoint][index[j]]; //新改进    								
    						vistpoint=index[j];  //重新设置要访问的点
    						break;
    					}    					
    				}    				
    			}
//    			if(midpointnum!=mid_num)
//    			{
//    				for(int j=0;j<mid_num;j++)
//    				{
//    					if(midvisited[midindex[j]]==1)
//    					{
//    						for(int k=0;k<601;k++)
//    						{
//    							map[k][midindex[j]]/=2;
//    						}
//    					}
//    				}
//    			}
    			if(!haschild)
    			{
    				continue;
    			}   			
				cost[i]+=map[vistpoint][end];  //这里先以路径最短为判断标准，后面要改
//    			cost[i]=midpointnum;
    			if(mincost>cost[i])
    			{
    				mincost=cost[i];
    				minpath=i;
    			}				
    		}
    		if(pathcost>mincost)
    		{
    			pathcost=mincost;
    			//path=isvistend[minpath];
    			path=edgevisited[minpath];
    		}
    		//蚂蚁的循环结束
    		//根据得到的最优解，来更新每一条边上的信息量，这里先以每次迭代的最短路径,同时也就局部的更新
    		if(minpath==-1)
    		{
    			iter++;
    			continue;
    		}
    			
    		for(int i=0;i<nodenum;i++)
    			for(int j=0;j<nodenum;j++)
    			{
//    				if(midpoint[j]==1)
//    					continue;
    				
//    				double temp=0;
//    				for(int k=0;k<antnum;k++)
//    				{
//        				if(isvistend[k].indexOf("|"+i+"|"+"|"+j+"|")>=0)
//        				{
//        					temp=temp+wconst/mincost;
//        				}   					
//    				}
//    				temp+=(bata)*inforC[i][j];
//    				inforC[i][j]=temp;
    				
    				double temp=(bata)*inforC[i][j];
    				if(isvistend[minpath].indexOf("|"+i+"|"+"|"+j+"|")>=0)
    				{
    					temp=temp+wconst/mincost;
    				}
    				if(temp<infmin)
    				{
    					temp=infmin;
    				}
    				else if(temp>infmax)
    				{
    					temp=infmax;
    				}
    				inforC[i][j]=temp;
    			}
    		iter++;
    	}
    	System.out.println(path);
    	return path;
    }
    
}