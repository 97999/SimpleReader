package com.example.simplereader.local;

import com.example.simplereader.R;

public class LocalTxt extends LocalFile{

    private int imageId;

    public LocalTxt(String bookName, String bookInfo, String bookPath){
        super(bookName, bookInfo, bookPath);
        this.imageId = R.drawable.ic_txt;
    }

    public int getImageId() {
        return imageId;
    }
}
