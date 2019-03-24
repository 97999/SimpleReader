package com.example.simplereader;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simplereader.adapter.FileRecyclerAdapter;
import com.example.simplereader.bookshelf.LocalBook;
import com.example.simplereader.local.LocalTxt;
import com.example.simplereader.local.LocalFile;
import com.example.simplereader.util.BookshelfHelper;
import com.example.simplereader.util.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TxtFileScanActivity extends BaseActivity {
    private List<LocalFile> txtList = new ArrayList<>();
    private FileRecyclerAdapter adapter;
    private TextView resultText;
    private int result = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_filescan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final String filePath = Environment.getExternalStorageDirectory().getPath();
        initViews();
        Thread thread =  new Thread(new Runnable() {
            @Override
            public void run() {
                txtList.clear();
                scanTxtFile(filePath);
            }});
        thread.start();
    }

    private void initViews(){
        resultText = findViewById(R.id.scan_result_text);
        adapter = new FileRecyclerAdapter(txtList);
        adapter.setOnItemClickListener(new FileRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                    LocalBook book = new LocalBook(txtList.get(position).getName(),
                            txtList.get(position).getPath());
                    //加入书架
                    switch (BookshelfHelper.getInstance().addLocalBook(book)){
                        case BookshelfHelper.ADD_FINISHED :
                            Toast.makeText(TxtFileScanActivity.this,
                                    "已加入书架", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case BookshelfHelper.ADD_FAILED :
                            Toast.makeText(TxtFileScanActivity.this,
                                    "添加失败", Toast.LENGTH_SHORT).show();
                            break;
                        case BookshelfHelper.BOOK_EXIST :
                            Toast.makeText(TxtFileScanActivity.this,
                                    "书籍已存在", Toast.LENGTH_SHORT).show();
                            break;

                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.txt_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.recycler_view_divider));
        recyclerView.addItemDecoration(itemDecoration);
    }

    /**
     * 递归遍历本地所有txt文件
     * @param filePath 需要扫描的目录
     */
    private void scanTxtFile(final String filePath){
        File file = new File(filePath);
        File[] files = file.listFiles();
        for(final File aFile : files){
            if(aFile.isDirectory()){
                scanTxtFile(aFile.getPath());
            } else {
                if(aFile.getName().endsWith(".txt")){
                    final String info = Utility.getFileLength(aFile.length());
                    result ++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultText.setText("共扫描到" + result + "本");
                            txtList.add(new LocalTxt(aFile.getName(), info, aFile.getPath()));
                            adapter.updateData(txtList);
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
                default:
        }
        return true;
    }
}
