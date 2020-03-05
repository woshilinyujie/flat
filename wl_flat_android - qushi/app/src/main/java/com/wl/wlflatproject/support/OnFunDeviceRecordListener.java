package com.wl.wlflatproject.support;



import com.wl.wlflatproject.support.models.FunDevRecordFile;

import java.util.List;

public interface OnFunDeviceRecordListener extends OnFunListener {

    void onRequestRecordListSuccess(List<FunDevRecordFile> files);

    void onRequestRecordListFailed(final Integer errCode);

}
