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



public final class Route
{
    /**
     * 你需要完成功能的入口
     *
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
    private static int[][] graph;
    private static int numVertex;
    private static int numDemand;
    private static int start,end;
    private static int Max_Val=12000;
    private static Map<String,Integer> map;
    private static boolean[] isDemand;
    private static Map<Integer,Integer> indexMap;
    private static int[][] path;
    private static int[][] dist;
    private static boolean[] visited;
    private static int[][] sorted;
    private static int[] tmpRet;
    private static int tmpRetCnt;
    private static List<Integer> finalList;
    private static long startTime;
    private static int finalCost;
    private static int tmpCost;
    private static boolean isFinish;
    private static int cnt; //记录demand的个数
    private static List<Integer> tmpList;
    private static boolean[] isReachEnd;
    private static int numUsedEnd;
    private static int totalEnd;
    public static String searchRoute(String graphContent, String condition)
    {
        startTime=System.currentTimeMillis();
//    	isFinish=false;
        String[] tmpSplit=graphContent.split("\n");
        if(tmpSplit.length<200) {
            init2(graphContent,condition);
            tmpList.add(start);
            visited[start]=true;
            DFS2(start);
        } else {
            init(graphContent, condition);
            tmpRet[tmpRetCnt++]=start;
            visited[start]=true;
            numUsedEnd=0;
            DFS(start);
        }
//    	测试代码，输出整个List
        System.out.println(finalCost);
        System.out.println(finalList);

        if(finalList.size()>0) {
            String result="";
            for(int i=0;i<finalList.size()-1;i++) {
                result+=map.get(String.valueOf(finalList.get(i).intValue())+","+
                        String.valueOf(finalList.get(i+1).intValue()));
                if((i+1)!=finalList.size()-1)
                    result+="|";
            }
            return result;
        } else {
            return "NA";
        }
    }
    // 初始化操作
    private static void init(String graphContent, String condition) {
        graph=new int[600][600];
        for(int i=0;i<600;i++)
            for(int j=0;j<600;j++)
                graph[i][j]=Max_Val;
        for(int i=0;i<600;i++)
            graph[i][i]=0;

        map=new HashMap<String,Integer>();
        String[] contentSplit=graphContent.split("\n");
        for(int i=0;i<contentSplit.length;i++) {
            String[] lineSplit=contentSplit[i].split(",");
            int number=Integer.valueOf(lineSplit[0]);
            int s=Integer.valueOf(lineSplit[1]);
            int d=Integer.valueOf(lineSplit[2]);
            int w=Integer.valueOf(lineSplit[3]);
            if(w<graph[s][d]) {	//去重复的值
                graph[s][d]=w;
                numVertex=numVertex>(s+1)?numVertex:(s+1);
                numVertex=numVertex>(d+1)?numVertex:(d+1);
                map.put(lineSplit[1]+","+lineSplit[2], number);
            }
        }
        isDemand=new boolean[numVertex];
        visited=new boolean[numVertex];
        for(int i=0;i<numVertex;i++) {
            visited[i]=false;
            isDemand[i]=false;
        }
        indexMap=new HashMap<Integer,Integer>();
        String subCondition=condition.substring(0, condition.length()-1);
        String[] conditionSplit=subCondition.split(",");
        start=Integer.valueOf(conditionSplit[0]);
        end=Integer.valueOf(conditionSplit[1]);
        String[] lineSplit=conditionSplit[2].split("\\|");
        numDemand=lineSplit.length;
        indexMap.put(start, 0);
        int[] startPoints=new int[numDemand+1];
        startPoints[0]=start;
        for(int i=0;i<lineSplit.length;i++) {
            int ver=Integer.valueOf(lineSplit[i]);
            isDemand[ver]=true;
            startPoints[i+1]=ver;
            indexMap.put(ver, i+1);
        }

        tmpRet=new int[numDemand+2];
        tmpRetCnt=0;
        finalList=new ArrayList<Integer>();
        finalCost=Integer.MAX_VALUE;
        tmpCost=0;
        totalEnd=0;
        isReachEnd=new boolean[numDemand+1];

        path=new int[numDemand+1][numVertex];
        dist=new int[numDemand+1][numVertex];
        //初始化最短路径表
        for(int i=0;i<startPoints.length;i++) {
            shortestPath(startPoints[i],indexMap.get(startPoints[i]),indexMap.get(startPoints[i]));
        }
        //测试代码，输出dist，path表
    /*	for(int i=0;i<startPoints.length;i++) {
    		System.out.print(startPoints[i]+"距离:");
    		for(int j=0;j<numVertex;j++) {
    			System.out.print(dist[indexMap.get(startPoints[i])][j]+"\t");
    		}
    		System.out.println();
    		System.out.print(startPoints[i]+"路径:");
    		for(int j=0;j<numVertex;j++) {
    			System.out.print(path[indexMap.get(startPoints[i])][j]+"\t");
    		}
    		System.out.println();
    	}*/
        //初始化sorted
        sorted=new int[numDemand+1][];
        int[] tmp=new int[numDemand];
        for(int i=0;i<numDemand;i++)
            tmp[i]=startPoints[i+1];
        for(int i=0;i<startPoints.length;i++) {
            int index=indexMap.get(startPoints[i]);
            int count=0;
            int minVal;
            int minIndex;
            for(int j=0;j<tmp.length;j++) {	//选择排序算法
                minVal=dist[index][tmp[j]];
                minIndex=j;
                for(int k=j+1;k<tmp.length;k++) {
                    if(dist[index][tmp[k]]<minVal)	{
                        minIndex=k;
                        minVal=dist[index][tmp[k]];
                    }
                }
                if(minVal<Max_Val) {
                    if(isAdd(startPoints[i],tmp[minIndex],visited)) {
                        if(minIndex==j) {
                            //如果相等，直接交换
                            int t=tmp[count];
                            tmp[count]=tmp[minIndex];
                            tmp[minIndex]=t;
                            count++;
                        } else {
                            //先交换j和count的
                            int t=tmp[j];
                            tmp[j]=tmp[count];
                            tmp[count]=t;
                            //再交换count和minIndex的
                            t=tmp[count];
                            tmp[count]=tmp[minIndex];
                            tmp[minIndex]=t;
                            count++;
                        }
                    } else {
                        dist[index][tmp[minIndex]]=Max_Val;
                        int t=tmp[minIndex];
                        tmp[minIndex]=tmp[j];
                        tmp[j]=t;
                    }
                } else {
                    break;
                }
            }
            if(startPoints[i]==start) {
                sorted[index]=new int[count];
                for(int j=0;j<count;j++)
                    sorted[index][j]=tmp[j];
            } else {
                sorted[index]=new int[count-1];
                for(int j=1;j<count;j++)
                    sorted[index][j-1]=tmp[j];
            }
            //重新初始化visited数组
            for(int k=0;k<numVertex;k++) {
                visited[k]=false;
            }
            visited[start]=true;
        }
        //初始化isReachEnd
        for(int i=0;i<numDemand+1;i++) {
            isReachEnd[i]=false;
        }
        for(int i=1;i<numDemand+1;i++) {
            if(dist[i][end]<Max_Val) {
                isReachEnd[i]=true;
                totalEnd++;
            }
        }
   /* 	//输出isReachEnd测试
    	for(int i=0;i<numDemand+1;i++) {
    		System.out.print(isReachEnd[i]+"\t");
    	}
    	System.out.println();*/
        //输出sorted的测试
    /*	for(int i=0;i<sorted.length;i++) {
    		System.out.print("顶点:");
    		for(int j=0;j<sorted[i].length;j++) {
    			System.out.print(sorted[i][j]+"\t");
    		}
    		System.out.println();
    		System.out.print("距离:");
    		for(int j=0;j<sorted[i].length;j++) {
    			System.out.print(dist[i][sorted[i][j]]+"\t");
    		}
    		System.out.println();
    	}*/
    }
    //
    private static boolean isAdd(int v0,int v1,boolean[] visited) {
        int tmpV=v1;
        int index=indexMap.get(v0);
        while(tmpV!=v0) {
            if(isDemand[tmpV]&&visited[tmpV])
                return false;
            tmpV=path[index][tmpV];
        }
        tmpV=v1;
        while(tmpV!=v0) {
            if(isDemand[tmpV])
                visited[tmpV]=true;
            tmpV=path[index][tmpV];
        }
        return true;
    }
    //求解Dijkstra最短距离
    private static void shortestPath(int v0,int pindex,int dindex) {
        int v,w,k=-1,min;
        boolean[] finalPoint=new boolean[numVertex];
        for(v=0;v<numVertex;v++) {   //初始化数据
            finalPoint[v]=false;
            dist[dindex][v]=graph[v0][v];
            path[pindex][v]=v0;
        }
        dist[dindex][v0]=0;
        path[pindex][v0]=-1;
        finalPoint[v0]=true;
        for(v=1;v<numVertex;v++) {
            min=Integer.MAX_VALUE;
            for(w=0;w<numVertex;w++) {
                if(!finalPoint[w]&&dist[dindex][w]<min) {
                    k=w;
                    min=dist[dindex][w];
                }
            }
            finalPoint[k]=true;
            for(w=0;w<numVertex;w++) {
                if(!finalPoint[w]&&(min+graph[k][w]<dist[dindex][w])) {
                    dist[dindex][w]=min+graph[k][w];
                    path[pindex][w]=k;
                }
            }
        }
    }
    //    检验函数
    private static boolean validate(int v0,int v1) {
        int index=indexMap.get(v0);
        boolean isValidate=true;
        int tmpV=v1;
        while(tmpV!=v0) {
            if(visited[tmpV]) {
                isValidate=false;
                break;
            }
            tmpV=path[index][tmpV];
        }
        List<Integer> tmpL=new ArrayList<Integer>();
        if(isValidate) {
            tmpV=v1;
            while(tmpV!=v0) {
                visited[tmpV]=true;
                if(isDemand[tmpV]) {
                    tmpL.add(tmpV);
                    if(isReachEnd[indexMap.get(tmpV)]) {
                        numUsedEnd++;
                    }
                }
                tmpV=path[index][tmpV];
            }
            for(int i=tmpL.size()-1;i>=0;i--)
                tmpRet[tmpRetCnt++]=tmpL.get(i);
            return isValidate;
        } else {
            return isValidate;
        }
    }
    //    回朔由validate产生的影响
    private static void antoValidate(int v0,int v1) {
        int index=indexMap.get(v0);
        int tmpV=v1;
        while(tmpV!=v0) {
            visited[tmpV]=false;
            if(isDemand[tmpV]) {
                tmpRetCnt--;
                if(isReachEnd[indexMap.get(tmpV)]) {
                    numUsedEnd--;
                }
            }

            tmpV=path[index][tmpV];
        }
    }
    //    深度遍历
    private static void DFS(int ver) {
        if(System.currentTimeMillis()-startTime>7.0*1000)
            return;
//    	if(isFinish)
//    		return;
        if((numUsedEnd>=totalEnd)&&(tmpRetCnt<numDemand+1)) {
            return;
        }
        if(tmpRetCnt==numDemand+1) {
            if(validate(ver,end) && (dist[indexMap.get(ver)][end]<Max_Val)) {
                tmpRet[tmpRetCnt++]=end;
                tmpCost+=dist[indexMap.get(ver)][end];
                //输出测试
                for(int i=0;i<tmpRetCnt;i++)
                    System.out.print(tmpRet[i]+" ");
                System.out.println();
                ///////////////////////
                if(tmpCost<finalCost) {
                    transfer();
                    finalCost=tmpCost;
                    isFinish=true;
                }
                tmpRetCnt--;
                antoValidate(ver,end);
            } else {
                return ;
            }
        } else {
            int index=indexMap.get(ver);
            for(int i=0;i<sorted[index].length;i++) {
                if((!visited[sorted[index][i]])&&validate(ver,sorted[index][i])) {
                    //预处理
//    				tmpRet[tmpRetCnt++]=sorted[index][i];
                    tmpCost+=dist[indexMap.get(ver)][sorted[index][i]];

                    //递归
                    if(tmpCost<finalCost)
                        DFS(sorted[index][i]);
                    //回朔
//    				tmpRetCnt--;
                    antoValidate(ver,sorted[index][i]);
                    tmpCost-=dist[indexMap.get(ver)][sorted[index][i]];
//    				if(isReachEnd[indexMap.get(sorted[index][i])]) {
//    					numUsedEnd--;
//    				}
                }
            }
        }
    }
    //转换
    private static void transfer() {
        finalList.clear();
        List<Integer> tmpList=new ArrayList<Integer>();
        finalList.add(start);
        for(int i=0;i<tmpRet.length-1;i++) {
            tmpList.clear();
            int index=indexMap.get(tmpRet[i]);
            int tmpV=tmpRet[i+1];
            while(tmpV!=tmpRet[i]) {
                tmpList.add(tmpV);
                tmpV=path[index][tmpV];
            }
            for(int j=tmpList.size()-1;j>=0;j--) {
                finalList.add(tmpList.get(j));
            }
        }
    }
    //对于点数较少的情况，直接回朔
    private static void init2(String graphContent, String condition) {
        graph=new int[600][600];
        for(int i=0;i<600;i++)
            for(int j=0;j<600;j++)
                graph[i][j]=Max_Val;
        for(int i=0;i<600;i++)
            graph[i][i]=0;

        map=new HashMap<String,Integer>();
        String[] contentSplit=graphContent.split("\n");
        for(int i=0;i<contentSplit.length;i++) {
            String[] lineSplit=contentSplit[i].split(",");
            int number=Integer.valueOf(lineSplit[0]);
            int s=Integer.valueOf(lineSplit[1]);
            int d=Integer.valueOf(lineSplit[2]);
            int w=Integer.valueOf(lineSplit[3]);
            if(w<graph[s][d]) {	//去重复的值
                graph[s][d]=w;
                numVertex=numVertex>(s+1)?numVertex:(s+1);
                numVertex=numVertex>(d+1)?numVertex:(d+1);
                map.put(lineSplit[1]+","+lineSplit[2], number);
            }
        }
        isDemand=new boolean[numVertex];
        visited=new boolean[numVertex];
        for(int i=0;i<numVertex;i++) {
            visited[i]=false;
            isDemand[i]=false;
        }
        String subCondition=condition.substring(0, condition.length()-1);
        String[] conditionSplit=subCondition.split(",");
        start=Integer.valueOf(conditionSplit[0]);
        end=Integer.valueOf(conditionSplit[1]);
        String[] lineSplit=conditionSplit[2].split("\\|");
        numDemand=lineSplit.length;
        for(int i=0;i<lineSplit.length;i++) {
            int ver=Integer.valueOf(lineSplit[i]);
            isDemand[ver]=true;
        }
        //变量初始化
        cnt=0;
        tmpList=new ArrayList<Integer>();
        finalList=new ArrayList<Integer>();
        finalCost=Integer.MAX_VALUE;
        tmpCost=0;
    }
    //    DFS2
    private static void DFS2(int v) {
        if(System.currentTimeMillis()-startTime>9.5*1000)
            return;

        for(int i=0;i<numVertex;i++) {
            if(visited[i])
                continue;
            if(graph[v][i]<Max_Val&&graph[v][i]>0) {
                if(tmpCost+graph[v][i]<finalCost) {
                    if((cnt<numDemand)&&(i==end)) {
                        continue;
                    } else if((cnt==numDemand)&&(i==end)) {
                        tmpCost+=graph[v][i];
                        tmpList.add(i);
                        finalList=new ArrayList<Integer>(tmpList);
                        finalCost=tmpCost;
                        //回朔
                        tmpCost-=graph[v][i];
                        tmpList.remove(tmpList.size()-1);
                        return;
                    } else {
                        tmpCost+=graph[v][i];
                        tmpList.add(i);
                        if(isDemand[i])
                            cnt++;
                        visited[i]=true;
                        //递归
                        DFS2(i);
                        //回朔
                        tmpCost-=graph[v][i];
                        tmpList.remove(tmpList.size()-1);
                        if(isDemand[i])
                            cnt--;
                        visited[i]=false;
                    }
                }
            }
        }
    }

}