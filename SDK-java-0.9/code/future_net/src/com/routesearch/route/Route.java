/**
 * 实现代码文件
 *
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;
import com.filetool.main.Main;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.LinkedList;

public final class Route
{
    /**
     * 你需要完成功能的入口
     *
     * @author XXX
     * @since 2016-3-4
     * @version 2016-4-9 14:39
     */
    private static final int myINF = 12000;
    private static final int ubound = 587;//
    private static int[][] graph = new int[605][605];
    private static int[][] vers2path = new int[605][605];
    private static int[][] v_edge = new int[605][605];
    private static int[] v_edge_num = new int[605];
    private static int ver_num;
    private static int edge_num;
    private static int start;
    private static  int end;
    private static int include_num;
    private static int[] include;
    private static boolean[] is_include = new boolean[605];
    private static int finalCost =myINF;
    private static LinkedList<Integer> tmpList = new LinkedList<Integer>();
    private static ArrayList<Integer> finalList = new ArrayList<>();
    private static boolean[] has_visited = new boolean[605];
    private static  long max_durtime = 9500;
    private static boolean excess_time = false;

    public static String searchRoute(String graphContent, String condition)
    {
        long StartTime = System.currentTimeMillis();
        String result = "";
        //read graph
        for(int i=0;i<605;++i){
            for(int j=0;j<605;++j){
                graph[i][j] = myINF;
            }
        }
        Arrays.fill(v_edge_num,0);
        String[] lines = graphContent.split("\n");
        edge_num = lines.length;
        ver_num = 0;
        for(String line:lines){
            String[] line_info = line.split(",");
            int path_id = Integer.valueOf(line_info[0]);
            int s = Integer.valueOf(line_info[1]);
            int d = Integer.valueOf(line_info[2]);
            int w = Integer.valueOf(line_info[3]);
            if(w < graph[s][d]){
                graph[s][d] = w;
                vers2path[s][d] = path_id;
                if(ver_num < s+1){
                    ver_num = s+1;
                }
                if(ver_num < d+ 1){
                    ver_num =d +1;
                }
                // add d to edge of s;
                int has_pos = -1;
                for(int i=0;i<v_edge_num[s];++i){
                    if(d == v_edge[s][i]){
                        has_pos = i;
                        break;
                    }
                }
                if(has_pos == -1){
                    v_edge[s][v_edge_num[s]] = d;
                    v_edge_num[s] += 1;
                }
            }
        }

        //read condition
        String[] cond_split = condition.split(",");
        start = Integer.valueOf(cond_split[0]);
        end = Integer.valueOf(cond_split[1]);
        String[] include_split = (cond_split[2]).split("\\|");
        include_num = include_split.length;
        include_split[include_num-1] = include_split[include_num-1].substring(0,include_split[include_num-1].length()-1);
        include = new int[include_num];
        for(int i=0;i<include_num;++i){
            int v = Integer.valueOf(include_split[i]);
            include[i] = v;
            is_include[v] = true;
        }

        //DFS
        finalCost = myINF;
        Arrays.fill(has_visited,false);
        tmpList.add(start);
        has_visited[start] = true;
        //for first five: edge_num < 200
        DFS(start, 0, 0);

        //GetResult
        if(finalList.size() > 0) {
            for (int i = 0; i < finalList.size() - 1; ++i) {
                result += String.valueOf(vers2path[finalList.get(i)][finalList.get(i + 1)]) + "|";
            }
            result = result.substring(0,result.length()-1);
        }else{
            result = "NA";
        }
        System.out.println(result);
        return result;
    }

    private static void DFS(int v, int cnt,int cost)
    {
        if(System.currentTimeMillis() - Main.start_time > max_durtime){
            excess_time = true;
            return;
        }
        if(cost > finalCost || cost > ubound){
            return;
        }
        if(v == end){
            if(cnt == include_num) {
                if (finalCost > cost) {
                    finalCost = cost;
                    finalList.clear();
                    for(int vex:tmpList){
                        finalList.add(vex);
                    }
                }
            }
            return;
        }
        int v_edge_end = v_edge_num[v];
        for(int i=0;i< v_edge_end;++i){
            int v_next = v_edge[v][i];
            if(has_visited[v_next]){
                continue;
            }
            tmpList.add(v_next);
            has_visited[v_next] = true;
            DFS(v_next,(is_include[v_next] ? cnt + 1 : cnt), cost + graph[v][v_next]);
            if(excess_time){
                break;
            }
            tmpList.removeLast();
            has_visited[v_next] = false;
        }
    }


}