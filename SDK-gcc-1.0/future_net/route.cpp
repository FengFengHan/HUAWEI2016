
#include "route.h"
#include "lib_record.h"
#include <stdio.h>
#include <stack>
#include <vector>
#include <string.h>
#include <cstdlib>
#include <string>
#include <iostream>
//#include "lp_lib.h"

using namespace std;

typedef  unsigned  short US;

US MaxDis = 22;
int graph[605][605];
int vers2path[605][605];
int v_to_edge[605][605];
int v_to_edge_num[605];
int to_v_edge[605][605];
int to_v_edge_num[605];
int ver_num;
int start;
int end;
int include_num;
int include[605];
bool is_include[605];
bool invalid[605];
US finalCost = 12000;
int valid_v_edge[605][605];
int valid_v_edge_num[605];
string lp_strs;

void GetInclude(char* str)
{
    const char * split = "|";

    char * p;

    p = strtok (str,split);

    include_num = 0;
    int v;
    while(p!=NULL) {
        v = atoi(p);
        include[include_num] = v;
        include_num++;
        is_include[v] = true;
        p = strtok(NULL,split);
    }
}

//你要完成的功能总入口
void search_route(char *topo[5000], int edge_num, char *demand)
{
    unsigned short result[] = {2, 6, 3};//示例中的一个解

    for (int i = 0; i < 3; i++)
        record_result(result[i]);
    //read demand
    int s, d;
    char include_str[1000];
    sscanf(demand,"%d,%d,%s",&s,&d,include_str);
    GetInclude(include_str);

    //read graph
    for(int i=0;i<=605;++i){
        for(int j=0;j<=605;++j){
            graph[i][j] = MaxDis;
        }
    }
    int p,v1,v2,w;
    for(int i=0;i<edge_num;++i){
        sscanf(topo[i],"%d,%d,%d,%d",&p,&v1,&v2,&w);
        if(w < graph[v1][v2]){
            if(v1 != d && v2 != s) {
                graph[v1][v2] = w;
                vers2path[v1][v2] = p;

                v_to_edge[v1][v_to_edge_num[v1]] = v2;
                v_to_edge_num[v1] = v_to_edge_num[v1]  + 1;

                to_v_edge[v2][to_v_edge_num[v2]] = v1;
                to_v_edge_num[v2] = to_v_edge_num[v2]  + 1;

            }
            if(ver_num < v1 + 1){
                ver_num = v1+1;
            }
            if(ver_num < v2 + 1){
                ver_num = v2 + 1;
            }
        }
    }
    //remove invalid point
    for(int i=0;i<=ver_num;++i){
        if(i == s || i == d){
            continue;
        }
        if(v_to_edge_num[i] == 0 || to_v_edge_num[i] == 0){
            invalid[i] = true;
        }
    }
    //

    //gen lps file
    lp_strs = "";
    //obj
    string lp_str_obj = "min: ";
    for(int i=0;i< ver_num;++i){
        if(invalid[i]){
            continue;
        }
        int v = i;
        char s[50];
        for(int j=0;j<v_to_edge_num[v];++j){
            int v_to = v_to_edge[v][j];
            if(invalid[v_to]){
                continue;
            }
            sprintf(s,"%d x%d%d + ", graph[v][v_to],v,v_to);
            lp_str_obj += s;
        }
    }
    lp_str_obj=lp_str_obj.substr(0,lp_str_obj.size()-3);
    lp_str_obj += ";\n";

    //st
    string lp_str_st = "";
    for(int i=0;i< ver_num;++i){
        if(invalid[i]){
            continue;
        }
        string lp_str_st_v_to = "";
        if(i != d){
            int v_to;
            char str[50];
            for (int j = 0; j < v_to_edge_num[i]; ++j) {
                v_to = v_to_edge[i][j];
                if(invalid[v_to]){
                    continue;
                }
                sprintf(str, "x%d%d + ", i, v_to);
                lp_str_st_v_to += str;
            }
            lp_str_st_v_to = lp_str_st_v_to.substr(0, lp_str_st_v_to.size() - 3);

            if(i == s || is_include[i]) {
                lp_str_st += lp_str_st_v_to + " = 1;\n";
            }else{
                lp_str_st += "0 <= " + lp_str_st_v_to + " <= 1;\n";
            }
        }

        string lp_str_st_to_v = "";
        if(i != s){
            int to_v;
            char str[50];
            bool on_nes = true;
            if(i != d && (!is_include[i])){
                on_nes = false;
            }
            for (int j = 0; j < to_v_edge_num[i]; ++j) {
                to_v = to_v_edge[i][j];
                if(invalid[to_v]){
                    continue;
                }
                if(on_nes) {
                    sprintf(str, "x%d%d + ", to_v, i);
                }else{
                    sprintf(str, "x%d%d - ", to_v, i);
                }
                lp_str_st_to_v += str;
            }
            lp_str_st_to_v = lp_str_st_to_v.substr(0, lp_str_st_to_v.size() - 3);

            if(i == d || is_include[i]) {
                lp_str_st += lp_str_st_to_v + " = 1;\n";
            }else{
                lp_str_st += lp_str_st_v_to + " - " + lp_str_st_to_v + " = 0;\n";
            }
        }

    }
    //type
    string lp_str_type = "\nbin ";
    for(int i=0;i< ver_num;++i){
        if(invalid[i]){
            continue;
        }
        for(int j=0;j< v_to_edge_num[i];++j){
            int v_to = v_to_edge[i][j];
            char str[50];
            if(invalid[v_to]){
                continue;
            }
            sprintf(str,"x%d%d, ",i,v_to);
            lp_str_type += str;
        }
    }
    lp_str_type = lp_str_type.substr(0,lp_str_type.size()-2);
    lp_str_type += ";\n";

    lp_strs = lp_str_obj + lp_str_st + lp_str_type;

    //cout << lp_strs;
    //opt
    FILE* fp = fopen("./lp_f.txt","w");
    fprintf(fp,"%s",lp_strs.c_str());
    fclose(fp);
    lprec *lp = read_LP(fp, NORMAL, "test model");
    solve(lp);


}
