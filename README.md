# NetControl
NetControl是一个用于连接并控制'低功耗蓝牙'的'安卓应用'，他支持连接并将蓝牙信息保存在以备再次使用。已连接的蓝牙可在蓝牙信息编辑界面选择[服务](https://www.bluetooth.com/specifications/gatt/)并进行操作，在指令框收发数据。<br>
<br>
系统需求: 安卓5.0以上<br>
权限需求: 蓝牙控制权限、位置权限<br>
下载地址: <br>
<br>
it is:<br>
* 界面简洁: 利用约束布局和动画布局将空间利用最大化<br>
* 操作简单: 只需打开扫描界面连接蓝牙，就能进行后续操作<br>
* 方便快捷: 将已连接蓝牙储存，不用再次配置。可自行添加快捷指令<br>

## Layout
应用布局主要在三个界面，其中主界面'MainActivity'包括了左侧抽屉布局的设置界面'SettingFragment'。扫描界面'ScanActivity'主要以列表的形式将蓝牙设备展示给用户，其中已保存蓝牙列表可进行编辑跳转。设备界面'DeviceActivity'主要用于设备信息展示。
<br>
* NetControl
    * MainActivity
        * 已连接设备列表
        * 蓝牙控制和指令框
        * 快捷指令列表
        * SettingFragment
            * 自动连接
            * 进制转换
            * 反馈设置
            * 帮助
    * DeviceActivity
        * 蓝牙信息、重命名和删除
        * 服务与特征
    * LaunchActivity
        * 初始化数据与配置
    * ScanActivity
        * 蓝牙扫描与已保存蓝牙查看
    * FirstActivity
        * 首次打开申请权限与教程

## BLE Control
