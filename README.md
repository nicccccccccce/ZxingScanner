#扫码
=====
###dependencies 中引用compile 'com.github.nicccccccccce.zxing:ZxingLib:1.0.4'
>
##调用
>
 Intent openCameraIntent = new Intent(this, SweepActivity.class);
>
        startActivityForResult(openCameraIntent, 0);
>
##返回结果
>
if (resultCode == 1001) {
>
         Bundle bundle = data.getExtras();
>
          String scanResult = bundle.getString("ercode");
>
    }
>
<img src="https://github.com/nicccccccccce/documents/blob/master/android-zxing.gif" height="50%" width="50%" />
