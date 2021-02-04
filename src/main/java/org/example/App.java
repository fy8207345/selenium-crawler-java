package org.example;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String PATH = "E:/tektutorialshub/";

    public static void main(String[] args) throws IOException{
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.tektutorialshub.com/angular-tutorial/");
        Stringholder stringholder = new Stringholder();
        stringholder.string = driver.getPageSource();

        Document document = Jsoup.parse(stringholder.string);
        filter(document);

        List<String> urls = new ArrayList<>();
        Evaluator.Tag article = new Evaluator.Tag("article");
        document.select(article).forEach(artic -> {
            Evaluator.Tag ol = new Evaluator.Tag("ol");
            artic.select(ol).forEach(o -> {
                Evaluator.Tag li = new Evaluator.Tag("li");
                o.select(li).forEach(l -> {
                    Evaluator.Tag a = new Evaluator.Tag("a");
                    l.select(a).forEach(aa -> {
                        String href = aa.attr("href");
                        urls.add(href);
                        String fileName = getFileName(href);
                        aa.attr("href", fileName);
                    });
                });
            });
        });

        stringholder.string = document.toString();

        File index = createDirAndFile(new File(PATH), "index.html");
        try (StringReader stringReader = new StringReader(stringholder.string); FileWriter fileWriter = new FileWriter(index)){
            IOUtils.copy(stringReader, fileWriter);
        }
        parseUrls(urls, driver, stringholder);
        driver.quit();
    }

    private static void filter(Document document) {
        Evaluator.Tag script = new Evaluator.Tag("script");
        Elements scripts = document.select(script);
        scripts.forEach(Node::remove);

        Evaluator.Tag iframe = new Evaluator.Tag("iframe");
        Elements iframes = document.select(iframe);
        iframes.forEach(Node::remove);

        Evaluator.Tag link = new Evaluator.Tag("link");
        Elements links = document.select(link);
        links.forEach(Node::remove);

        Evaluator.Tag id = new Evaluator.Tag("cookie-notice");
        Elements ids = document.select(id);
        ids.forEach(Node::remove);
    }

    private static String getFileName(String url) {
        String replace = url.replace("https://www.tektutorialshub.com/", "");
        String fileName = "";
        if(replace.endsWith("/")){
            fileName = replace.substring(0, replace.length()-1) + ".html";
        }else {
            fileName = replace + ".html";
        }
        return fileName;
    }

    private static void parseUrls(List<String> urls, WebDriver driver, Stringholder stringholder){
        urls.forEach(url -> {
            driver.get(url);
            stringholder.string = driver.getPageSource();

            Document document = Jsoup.parse(stringholder.string);
            filter(document);

            stringholder.string = document.toString();

            String replace = url.replace("https://www.tektutorialshub.com/", "");
            String[] split = replace.split("/");
            try {
                File file = createDirAndFile(new File(PATH + split[0]), split[1] + ".html");
                try (StringReader stringReader = new StringReader(stringholder.string); FileWriter fileWriter = new FileWriter(file)){
                    IOUtils.copy(stringReader, fileWriter);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static File createDirAndFile(File dir, String fileName) throws IOException {
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        if(!file.exists()){
            file.createNewFile();
        }
        return file;
    }

    private static class Stringholder{
        private String string;
    }
}
