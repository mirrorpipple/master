#
# EDINETから各企業のXBRLファイルをDLする
#
# usage:
#     download_xbrl_from_edinet.py yyyymmdd

import scrapy, logging, re, urllib, os, zipfile, sys, random, time, csv
from scrapy.crawler import CrawlerProcess

class EdinetSpider(scrapy.Spider):
    name = 'edinet_spider'
    allowed_domains = ['edinet-fsa.go.jp']
    max_page = 1 # 有報が落とせる最大ページ数
    this_page = 0 # 今読み込んでいるページ
    
    url_soil = 'https://disclosure.edinet-fsa.go.jp/E01EW/BLMainController.jsp?uji.verb=W1E63021CXP002005BLogic&uji.bean=ee.bean.parent.EECommonSearchBean&TID=W1E63021&lgKbn=2&pkbn=0&skbn=0&' \
               'dskb=&dflg=0&iflg=0&preId=1&row=100&syoruiKanriNo=&sec=&scc=&snm=&spf1=1&spf2=1&iec=&icc=&inm=&spf3=1&fdc=&fnm=&spf4=1&spf5=2&cal=1&era=H&yer=&mon=&cal2=2&psr=2&' \
               'idx={0}&otd=120&yer2={1}&mon2={2}&day2={3}&yer3={1}&mon3={2}&day3={3}'

    def __init__(self, t_ymd):
        self.t_y = int(t_ymd[0:4])
        self.t_m = int(t_ymd[4:6])
        self.t_d = int(t_ymd[6:8])
        self.start_urls = [self.url_soil.format(self.this_page * 100,self.t_y,self.t_m, self.t_d)]

    # HTMLからぶっこ抜く
    def parse(self, response):

        # 抜いたHTMLをセレクタへキャッシュ
        res = scrapy.Selector(response)

        # 次ページへのリンクテキスト入手 ※idxに数値を指定すると、そこから100件表示してくれる
        list_pages = res.xpath('//p[@class="pageLink" and @id="pageTop"]/span/a/text()').extract()
        # リンクが飛べればリストの最後から2つ目に最大値が入っているはずなので、そこから最大ページを取得する
        if len(list_pages) > 2:
            self.max_page = int(list_pages[-2])
        
        # PIDとSESSIONKEYをFORMから取得する
        p_id = res.xpath('//form/input[@name="PID"]/@value').extract()
        session_key = res.xpath('//form/input[@name="SESSIONKEY"]/@value').extract()
        
        # ページに表示されている全てのtr（有報のリスト）を拾う
        list_tr = res.xpath('//table[@class="resultTable table_cellspacing_1 table_border_1 mb_6"]/tr[not(re:test(@class, "tableHeader"))]')

        # 全ての有報リストから、1つずつ企業情報とXBRLのDLリンクを整理する
        for tr_attr in list_tr:
            filling_date = tr_attr.xpath('.//td[1]/div/text()').extract_first()
            filling_date = modify_the_date_in_ymd(filling_date)
            yuho_title = tr_attr.xpath('.//td[2]/a/text()').extract_first()
            yuho_title = re.sub('[\s－\(\)]', '', yuho_title)
            edinet_code = tr_attr.xpath('.//td[3]/div/text()').extract_first()
            edinet_code = re.sub('[\s 　/]', '', edinet_code)
            xbrl_attr =  tr_attr.xpath('.//td[7]/div/a/@onclick').extract_first()
            
            # 企業名はtdにあるときと、aにあるときと2種類がある
            company_name = tr_attr.xpath('.//td[4]/text()').extract_first()
            company_name = re.sub('[\s	/]', '', company_name) # ブランクが紛れ込んでいて、そのままだと空白チェックに使えない
            if len(company_name) == 0:
                company_name = tr_attr.xpath('.//td[4]/a/text()').extract_first()
                company_name = re.sub('[\s	/]', '', company_name)

            # xbrlファイルがない場合、または、"修正有価証券報告書"だった場合はskipする
            has_dl_blogic = ""
            has_file_id = ""
            if xbrl_attr != None and ("修正有価証券報告書" not in yuho_title or "確認書" not in yuho_title):
                # control_keyを取得する
                has_dl_blogic = re.search("downloadFile\(\'(.+?)\'\,", xbrl_attr)
                # file_idを取得する
                has_file_id = re.search("no\=(.+?)\&", xbrl_attr)

            # dl_blogicとfile_idが取得できたらDLを開始する
            if has_dl_blogic and has_file_id:
                dl_blogic = has_dl_blogic.group(1)
                file_id = has_file_id.group(1)
                
                # レコードのprofileを生成
                xbrl_profile = [filling_date, yuho_title, edinet_code, company_name]
                folder_name = re.sub(r'\s', '', (filling_date + "_" + company_name))

                # xbrlをDL
                xbrl_download(p_id, session_key, dl_blogic, file_id, folder_name, xbrl_profile)
                # ファイルDLは休憩しながら
                time.sleep(random.randint(2, 5))

        # 次のページがあれば飛んで、XBRLを落とす
        # TODO: 毎ページでmax_page取得してるので結果的に正常に動いているけど、本当はインクリメントで動くようにするべき
        if self.max_page > self.this_page:
            self.this_page +=1
            next_page = self.url_soil.format(self.this_page * 100, self.t_y, self.t_m, self.t_d)
            yield scrapy.Request(next_page, callback=self.parse)

# --- 関数の定義 ---
def modify_the_date_in_ymd(filling_date):
    filling_date = re.sub('[\s :]', '.', filling_date).replace(u'\xa0', u' ').replace(u' ', '.') # 一度unicodeのブランクに直す必要がある
    list_ymd = filling_date.split('.')
    list_ymd = [x for x in list_ymd if len(x) != 0]

    if list_ymd[0][0] =='H':
        year = 1988 + int(list_ymd[0].replace('H', ''))
    else:
        year = 'unknown'
    return (str(year) + list_ymd[1] + list_ymd[2] + list_ymd[3] + list_ymd[4])

def xbrl_download(p_id, session_key, dl_blogic, file_id, folder_name, xbrl_profile):
    # httpリクエストを生成
    request_url = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb={0}&uji.bean=ee.bean.parent.EECommonSearchBean&PID={1}&SESSIONKEY={2}&no={3}".format(dl_blogic, p_id, session_key, file_id)

    # zipファイルの名前をセット
    zip_file_name = "xbrls/temp.zip"

    # urllibを使用して、zipをDL
    file_download = urllib.request.urlopen(request_url)
    data = file_download.read()
    with open(zip_file_name, "wb") as file:
        file.write(data)

    # 落としてきたzipを解凍
    zip_ref = zipfile.ZipFile(zip_file_name, 'r')
    zip_ref.extractall("xbrls/" + folder_name)
    zip_ref.close()

    # zipファイルを削除
    os.remove(zip_file_name)
    print("download XBRL of is successful : " + folder_name)

    # XBRLのDL元を記載したmanifest.csvを作る
    with open('xbrls/{0}/manifest.csv'.format(folder_name), 'w', newline='', encoding='utf8') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(xbrl_profile)

# メイン処理
if len(sys.argv) == 2 and sys.argv[1].isdigit() and len(sys.argv[1]) == 8:
    # UserAgentを書き開ける + 複数実行になるため、Telnet Consoleを停止
    process = CrawlerProcess({
        'USER_AGENT': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36', 'TELNETCONSOLE_ENABLED': False
              })

    logging.getLogger('scrapy').setLevel(logging.ERROR)
    process.crawl(EdinetSpider, str(sys.argv[1]))
    process.start()
else:
    print('引数：yyyymmdd　で取得したい日付を指定してください')


