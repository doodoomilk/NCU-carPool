# NCU-carPool
My independent study in NCU

See more information in [Wiki](https://github.com/doodoomilk/NCU-carPool.wiki.git) page

## Introduction of Code 
### MainActivity.java
- Tab setting: 切換分頁
- Floating button: 刊登共乘用
- NavigationView: 側邊欄setting
- DrawerLayout: 設定整個畫面(讓他可放入側邊欄跟tab)
- Relative xml:
    1. activity_main.xml
    2. nav_header_main.xml
    3. content_main.xml (tab有關)
    4. app_bar_main.xml (floating button)
    5. dialog_info.xml (查看個人資料用)

### LoginActivity.java
- SharedPreferences: 記住登入狀態
- Login GUI setting
- Relative xml:
    1. activity_login.xml

### RegisterActivity.java
- the registration page
- Pattern: regular expression
- Relative xml:
    1. activity_register.xml

### add_new_missionActivity.java
- Used to post new carpool mission
- Relative xml:
    1. activity_add_new_mission.xml

### misstion_list.java
- the 1st tab: show the list of missions
- RecyclerView: 放cardview的地方
- CardView: 顯示一個一個的mission
- Dialog: used to show the details of mission
- Sorting: the newest to the oldest
- RefreshLayout: 實現下拉刷新功能
- Relative xml:
    1. mission_list.xml
    2. few_detail.xml
    3. dialog_all_detail.xml

### my_list.java
- show carpool mission I launched or I joined
- RecyclerView: 放cardview的地方
- CardView: 顯示一個一個的mission
- Dialog: used to show the details of mission
- Sorting: the newest to the oldest
- RefreshLayout: 實現下拉刷新功能
- Relative xml:
    1. my_list.xml
    2. dialog_join_detial.xml
    3. dialog_launch_detial.xml
    4. dialog_people.xml
    5. join_few_detail.xml
    6. launch_few_detail.xml

### join_rateActivity.java
- the rating system of carpool launcher
- RatingBar: 評分星等
- Relative xml:
    1. activity_join_rate.xml

### rateActivity.java, rateActivity2.java, rateActivity3.java
- the rating system of carpool passengers
- RatingBar: 評分星等
- Relative xml:
    1. activity_rate.xml
    2. activity_rate2.xml
    3. activity_rate3.xml


