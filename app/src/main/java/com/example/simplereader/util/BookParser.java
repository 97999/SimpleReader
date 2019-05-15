package com.example.simplereader.util;

import android.util.Log;

import com.example.simplereader.sitebean.Book;
import com.example.simplereader.sitebean.BookMsg;
import com.example.simplereader.sitebean.Chapter;
import com.example.simplereader.sitebean.UpdateMsg;
import com.example.simplereader.siteparser.Binghuo;
import com.example.simplereader.siteparser.Luoqiu;
import com.example.simplereader.siteparser.WebSite;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单例模式
 */
public class BookParser {

    private int MAX_REQUEST = 3;    //请求失败的最大网络请求数
    private int count = 0;    //当前请求失败计数器

    private static BookParser instance;

    private Map<String, WebSite> map = new HashMap<>();

    private List<Chapter> catalog;    //目录
    private String source;     //来源网站

    private ExecutorService threads;


    private BookParser(){
        map.put("冰火中文", new Binghuo());
        map.put("落秋中文", new Luoqiu());
    }

    public static BookParser getInstance(){
        if(instance == null){
            synchronized (SpHelper.class){
                if(instance == null){
                    instance = new BookParser();
                }
            }
        }
        return instance;
    }

    /**
     * 加载目录
     * @param url  地址
     * @param source   来源网站
     * @param callBack    回调接口
     */
    public void loadCatalog(String url, String source, StateCallBack callBack){
        WebSite site = map.get(source);
        this.source = source;
        if(site != null){
            Log.d("BookParser", url);
            new Thread(() -> site.getCatalog(url, new StateCallBack<List<Chapter>>() {
                @Override
                public void onSuccess(List<Chapter> chapters) {
                    catalog = chapters;
                    //通知其调用者加载目录完成
                    callBack.onSucceed();
                }

                @Override
                public void onSucceed() {

                }

                @Override
                public void onFailed(String s) {
                    count++;
                    if(count < MAX_REQUEST){
                        site.getCatalog(url, this);
                    } else {
                        count = 0;
                        callBack.onFailed(s);
                    }
                }
            })).start();
        }
    }

    /**
     * 返回目录
     * @return  目录
     */
    public List<Chapter> getCatalog(){
        return catalog;
    }

    /**
     * 返回章节内容
     * @param index   章节在目录中的索引号
     * @param callBack    回调接口
     */
    public void getContent(int index, StateCallBack<String> callBack){
        WebSite site = map.get(source);
        if(site != null){
            new Thread(() -> site.getContent(catalog.get(index).getUrl(), callBack)).start();
        }
    }

    /**
     * 返回书本信息
     * @param url   地址
     * @param source   来源网站
     * @param callBack    回调接口
     */
    public void getBookInfo(String url, String source, StateCallBack<UpdateMsg> callBack){
        WebSite site = map.get(source);
        if(site != null)
            new Thread(() -> site.getBookInfo(url, callBack)).start();
    }

    /**
     * 返回书本更新信息
     * @param url   地址
     * @param source    来源网站
     * @param callBack    回调接口
     */
    public void getUpdate(String url, String source, StateCallBack<UpdateMsg> callBack){
        WebSite site = map.get(source);
        if(site != null)
        new Thread(() -> site.getUpdateInfo(url, callBack)).start();
    }

    /**
     * 根据书名或者作者名搜索
     * @param name   关键字
     * @param callBack    回调接口
     */
    public void search(String name, StateCallBack<Book> callBack){
        Collection<WebSite> sites = map.values();
        threads = Executors.newFixedThreadPool(sites.size());
        for(WebSite site : sites){
            threads.execute(() -> site.search(name, callBack));
        }
    }

    /**
     * 停止搜索，防止内存泄露
     */
    public void destroySearch(){
        if(threads != null){
            threads.shutdownNow();
        }
    }

}
