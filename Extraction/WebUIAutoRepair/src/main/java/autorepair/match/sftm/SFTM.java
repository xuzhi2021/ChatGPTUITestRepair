package autorepair.match.sftm;

import autorepair.match.sftm.treematching.Edge;
import autorepair.match.sftm.utils.TreeMatcher;
import autorepair.match.sftm.treematching.TreeMatcherResponse;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SFTM {

    public TreeMatcherResponse getTreeMatcherResponse() {
        return treeMatcherResponse;
    }

    public void setTreeMatcherResponse(TreeMatcherResponse treeMatcherResponse) {
        this.treeMatcherResponse = treeMatcherResponse;
    }

    private TreeMatcherResponse treeMatcherResponse;

    public static void main(String[] args) throws Exception {
        SFTM sftm = new SFTM();
        sftm.matchByPath("D:\\rep\\WebUIAutoRepair\\output\\old\\testcases\\mantisbt\\model_based_dataset\\AddProfileTest\\1\\temp.html",
                "D:\\rep\\WebUIAutoRepair\\output\\new\\testcases\\mantisbt\\model_based_dataset\\AddProfileTest\\1\\temp.html");
        for (Edge edge : sftm.getTreeMatcherResponse().getEdges()){
            if (edge.getSource()!=null){
                System.out.println(edge.getSource().getXPath());
            }
        }
//        String html = readFileAsString("D:\\rep\\WebUIAutoRepair\\output\\old\\testcases\\mantisbt\\model_based_dataset\\AddProfileTest\\1\\temp.html");
//        Document document = Jsoup.parse(html);
//        Element elemnt =document.getElementsByTag("body").get(0);
//        for (Element child : elemnt.children()){
//            System.out.println(child.tagName());
//            for(Element cc : child.children()){
//                System.out.println("     "+ cc.tagName());
//            }
//        }

    }

    public void match(String oldHtml, String newHtml){
        treeMatcherResponse = TreeMatcher.matchWebpages(oldHtml,newHtml);
        System.out.println("*****************");
        System.out.println(treeMatcherResponse.getEdges().size());
    }

    public void matchByPath(String oldHtmlPath, String newHtmlPath) throws Exception {
        String oldHtml = readFileAsString(oldHtmlPath);
        String newHtml = readFileAsString(newHtmlPath);
        match(oldHtml, newHtml);
    }

    public static String readFileAsString(String fileName)throws Exception{
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    public String getNewXpath(String oldXpath){
        for (Edge edge : treeMatcherResponse.getEdges()){
            if (edge.getSource() == null) continue;
            if (normalizeXpath(edge.getSource().getXPath()).equals(oldXpath)){
                if (edge.getTarget() == null){
                    return "";
                }else {
                    return normalizeXpath(edge.getTarget().getXPath());
                }
            }
        }
        return "";
    }

    public String getOldXpath(String newXpath){
        for (Edge edge : treeMatcherResponse.getEdges()){
            if (edge.getTarget() == null) continue;
            if (newXpath.equals(normalizeXpath(edge.getTarget().getXPath()))){
                if (edge.getSource() == null){
                    return "";
                }else {
                    return normalizeXpath(edge.getSource().getXPath());
                }
            }
        }
        return "";
    }

    public String normalizeXpath (String erratumXpath){
        if (erratumXpath == null) return null;
        String headXpath = "/html[1]/body[1]";
        StringBuilder xpath = new StringBuilder( erratumXpath.substring(5));
        String[] partXpaths = xpath.toString().split("/");
        String postXpath = "";
        for (String partXpath : partXpaths){
            if (partXpath.trim().length() == 0) continue;
            postXpath += "/";
            String temp = partXpath.substring(partXpath.indexOf("[") + 1,partXpath.lastIndexOf("]"));
            int index = Integer.parseInt(temp);
            index++;
            if (!partXpath.substring(0, partXpath.indexOf("[")).equals("svg")){
                postXpath = postXpath  + partXpath.substring(0,partXpath.indexOf("["))+ "[" + index + "]";
            }else {
                postXpath = postXpath + "/*[name()=\"svg\"][" + index +"]";
            }
        }
        return headXpath + postXpath;
    }
}
