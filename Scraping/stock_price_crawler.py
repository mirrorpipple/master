#
# 銘柄の株価CSVをWEBページからDLする
#
# usage:
#     stock_price_crawler.py yyyy-mm-dd
#
#     引数を指定しない場合は、実行時のシステム日付を使用します。
#     例： stock_price_crawler.py 2017—01-01
#

import os, sys, datetime, csv
from time import sleep
from selenium import webdriver

# WEBサイトから株価データをDLする
def download_stock_price(download_dir, download_file_path, this_day):
    # プロファイルをセット
    chrome_opt = webdriver.ChromeOptions()
    prefs = {"download.default_directory" : download_dir}
    chrome_opt.add_experimental_option("prefs",prefs)

    #seleniumでChromeを起動
    driver = webdriver.Chrome(chrome_options=chrome_opt)
    driver.get("http://k-db.com/stocks/{0}".format(this_day))

    s_date = driver.find_element_by_xpath("//div[@id='tablecaption']").text
    a_link_obj = driver.find_element_by_xpath("//div[@id='downloadlink']/a")

    # 日付が異なっていたら、そこで処理を返却する
    txt_date = this_day.split("-")
    txt_date = "{0}年{1}月{2}日".format(txt_date[0], txt_date[1], txt_date[2])

    # HPから取得してきた日付と、指定日付が一致しない場合終了させる。(存在しない日付の場合最新日のページが表示される）
    if not s_date.count(txt_date):
        driver.close()
        return False

    # linkが存在してDLが始まっていれば、完了確認してからブラウザを閉じる
    if a_link_obj != None:
        a_link_obj.click()
        while True:
            sleep(1)
            if os.path.exists(download_file_path):
                break
    
    # ブラウザを閉じる
    driver.close()
    return True

# DLしたCSVに日付カラムを追加して、集合データへappendする
def append_csv(download_file_path, output_file_path, this_day):   #defは関数の定義  applend_csvは三つの要素を持っている。
    # 日付をyyyy-mm-dd から、yyyy/mm/ddへ変換する (0始まりなら0を消す　例：2017/11/1)
    day_splited = this_day.split("-")
    day_rtval = "{0}/{1}/{2}".format(day_splited[0], str(int(day_splited[1])), str(int(day_splited[2])))

    # 集合データを開く
    with open(output_file_path, 'a', newline='', encoding="Shift-JIS") as out_f:
        writer = csv.writer(out_f, lineterminator='\n')
        # さっきDLしたCSVを読み込む
        with open(download_file_path, 'r', encoding="Shift-JIS", errors="ignore") as in_f:
            reader = csv.reader(in_f)
            header = next(reader)  # ヘッダーを読み飛ばす
            for row in reader:
                o_txt = [day_rtval] + row
                writer.writerow(o_txt)

# DLの対象日付として、引数にセットされた日付またはシステム日付を使用する
if len(sys.argv) > 1:
    this_day = sys.argv[1]
else:
    this_day = datetime.datetime.today().strftime("%Y-%m-%d")  #入力無かったら今日の日付がthis_day

# 格納先のパスをセット
home_dir="<HOME_DIR>"
download_file_path = "{0}/raw_data/stocks_{1}.csv".format(home_dir, this_day)
output_file_path = "{0}/all_stock_price.csv".format(home_dir)

# DL処理を呼び出し
has_downloaded = download_stock_price("{0}/raw_data/".format(home_dir), download_file_path, this_day)

# DLに成功していたらファイルをまとめる
if has_downloaded:
    append_csv(download_file_path, output_file_path, this_day)

