扫码
=====
用法说面
-----
调用
-----
>
 Intent openCameraIntent = new Intent(this, SweepActivity.class);
>
        startActivityForResult(openCameraIntent, 0);
>
返回结果
-----
>
if (resultCode == 1001) {
 >
         Bundle bundle = data.getExtras();
 >
          String scanResult = bundle.getString("ercode");
 >
    }
