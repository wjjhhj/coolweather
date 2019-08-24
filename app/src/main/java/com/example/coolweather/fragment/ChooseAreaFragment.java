package com.example.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.R;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtils;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE=0;
    private static final int LEVEL_CITY=1;
    private static final int LEVEL_COUNTRY=2;
    private ProgressDialog mProgressDialog;
    private TextView tvTitle;
    private ImageView ivBack;
    private ListView mListView;
    private ArrayAdapter<String> adapter;
    private List<String>dataList=new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province>mProvinceList;
    /**
     * 市列表
     */
    private List<City>mCityList;
    /**
     * 县列表
     */
    private List<Country>mCountryList;
    //选中的省
    private Province selectedProvince;
    //选中的市
    private City selectedCity;
    //选中的县
    private Country selectedCountry;
    //当前选中的等级
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View mView;
         mView=inflater.inflate(R.layout.choose_area,container,false);
        tvTitle=mView.findViewById(R.id.text_title);
        ivBack=mView.findViewById(R.id.iv_back);
        mListView=mView.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(adapter);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=mProvinceList.get(i);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=mCityList.get(i);
                    queryCountry();
                }else if (currentLevel==LEVEL_COUNTRY){
                    String weatherId=mCountryList.get(i).getWeatherId();
                    Intent intent=new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }




        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTRY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvince();
                }
            }
        });
        queryProvince();
    }
    /**
     * 查询所有的省，优先从数据库中查询，如果没有，就从服务器上查询
     */
    private void queryProvince() {
        tvTitle.setText("中国");
        ivBack.setVisibility(View.GONE);
        mProvinceList= DataSupport.findAll(Province.class);
        if (mProvinceList.size()>0){
            dataList.clear();
            for (Province province:mProvinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
    }

    /**
     * 查询省内所有的市，优先从数据库中查询，如果没有查询到，再从服务器查询
     */
    private void queryCities() {
        tvTitle.setText(selectedProvince.getProvinceName());
        ivBack.setVisibility(View.VISIBLE);
        mCityList=DataSupport.where("provinceid=?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (mCityList.size()>0){
            dataList.clear();
            for (City city:mCityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromService(address,"city");
        }
    }

    /**
     * 查询省内所有的县，优先从数据库中查询，如果没有查询到，再从服务器查询
     */

    private void queryCountry() {
        tvTitle.setText(selectedCity.getCityName());
        ivBack.setVisibility(View.VISIBLE);
        mCountryList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
        if (mCountryList.size() > 0) {
            dataList.clear();
            for (Country country : mCountryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromService(address,"country");
        }

    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     *
     */
    private void queryFromService (String address, final String type){
        showProgressDialog();
        HttpUtils.SendOkhttpRequst(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            String responseText=response.body().string();
            boolean result=false;
            if ("province".equals(type)){
                result= Utility.handleProvinceResponse(responseText);
            }else if ("city".equals(type)){
                result=Utility.handleCityResponse(responseText,selectedProvince.getId());
            }else if ("country".equals(type)){
                result=Utility.handleCountryResponse(responseText,selectedCity.getId());
            }
            if (result){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        if ("province".equals(type)){
                            queryProvince();
                        }else if ("city".equals(type)){
                            queryCities();
                        }else if ("country".equals(type)){
                            queryCountry();
                        }
                    }
                });
            }
            }


        });
    }

    /**
     * 显示对话框
     */
    private void showProgressDialog() {
        if(mProgressDialog==null){
            mProgressDialog=new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    /**
     * 消除对话框
     */
    private void closeProgressDialog() {
        if (mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }
}
