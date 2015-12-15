package com.unbank.classily;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ClassifierService {

	private static String userDefineWords = "userDefine.dic";
	private static String wordFilePath = "wordsDic.txt";
	private static String wordfeaFilePath = "feaWordsDic.txt";
	private static String keywordsFilePath = "keywords.txt";
	private static String topicModelFilePath = "topicModel.txt";
	private static String SoftmaxModelFilePath = "SoftmaxModel22.txt";

	public static Map<String, Integer> word2Index;
	public static List<Integer> wordsFeature;
	public static Map<Integer, String> keywords;
	private static List<Tmodel> topicModelParam;
	private static SoftmaxModelResult SoftmaxModelParam;
	private static int K;
	private static int n_in;
	private static int n_out;

	/**
	 * 系统启动初始化
	 */
	public void init() {
		Gson gson = new Gson();
		// System.out.println("------------------------>>>"+userDefineWords);
		/**
		 * 加载基础词典
		 */
		String words = FileReader(wordFilePath);
		word2Index = gson.fromJson(words,
				new TypeToken<Map<String, Integer>>() {
				}.getType());
		/**
		 * 加载词特称
		 */
		String wordfea = FileReader(wordfeaFilePath);
		wordsFeature = gson.fromJson(wordfea, new TypeToken<List<Integer>>() {
		}.getType());

		/**
		 * 1.加载topic model 参数
		 */
		String topicModel = FileReader(topicModelFilePath);
		topicModelParam = gson.fromJson(topicModel,
				new TypeToken<List<Tmodel>>() {
				}.getType());
		K = topicModelParam.size();

		/**
		 * 2.加载softmax model 参数
		 */
		String softmaxModel = FileReader(SoftmaxModelFilePath);
		SoftmaxModelParam = gson.fromJson(softmaxModel,
				new TypeToken<SoftmaxModelResult>() {
				}.getType());
		n_in = SoftmaxModelParam.getW()[0].length;
		n_out = SoftmaxModelParam.getIndex2Class().size();
		/**
		 * 3.加载用户自定义词典 （指定不可分割的词）
		 */
		addUserDefineWords();
		/**
		 * 4.加载keywords词典
		 */
		String keywordsFile = FileReader(keywordsFilePath);
		keywords = gson.fromJson(keywordsFile,
				new TypeToken<Map<Integer, String>>() {
				}.getType());
		/**
		 * 5.启动分词器
		 */
		ToAnalysis.parse("");
	}

	/**
	 * 文件读取器
	 * 
	 * @param path
	 * @return
	 */
	private String FileReader(String path) {
		String result = null;
		BufferedReader br = null;
		try {
			String classPath = ClassifierService.class.getClassLoader()
					.getResource("").toURI().getPath();
			File fileTarget = new File(classPath + path);
			if (fileTarget.exists()) {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(fileTarget), "utf-8"));
				result = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 添加用户词典
	 */
	public static void addUserDefineWords() {
		BufferedReader br = null;
		try {
			String classPath = ClassifierService.class.getClassLoader()
					.getResource("").toURI().getPath();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					classPath + userDefineWords), "utf-8"));
			/**
			 * 一行为一个自定义词
			 */
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				UserDefineLibrary.insertWord(line.trim(), "userDefine", 1000);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 文本切词处理
	 * 
	 * @param content
	 * @return
	 */
	public Map<Integer, Integer> cutWords(String content) {
		// 分词处理
		List<Term> termList = ToAnalysis.parse(content);
		// c ,m,d,f,p,r ,ud,y,x,uv,uj,ug,ud
		Map<Integer, Integer> wordFeature = new HashMap<Integer, Integer>();
		for (Term t : termList) {
			if (t.getNatureStr().startsWith("c")
					|| t.getNatureStr().startsWith("m")
					|| t.getNatureStr().startsWith("d")
					|| t.getNatureStr().startsWith("f")
					|| t.getNatureStr().startsWith("p")
					|| t.getNatureStr().startsWith("r")
					|| t.getNatureStr().startsWith("ud")
					|| t.getNatureStr().startsWith("y")
					|| t.getNatureStr().startsWith("x")
					|| t.getNatureStr().startsWith("uv")
					|| t.getNatureStr().startsWith("uj")
					|| t.getNatureStr().startsWith("ug")
					|| t.getNatureStr().startsWith("ud")) {
				continue;
			}
			String key = t.getName();
			// 词典过滤
			if (word2Index.containsKey(key)) {
				Integer wordId = word2Index.get(key);
				if (wordFeature.containsKey(wordId)) {
					wordFeature.put(wordId, wordFeature.get(wordId) + 1);
				} else {
					wordFeature.put(wordId, 1);
				}
			}
		}
		return wordFeature;
	}

	/**
	 * 词特称-主题特称映射
	 * 
	 * @return
	 */
	public double[] topicFeature(Map<Integer, Integer> wordFeature,
			StringBuffer keyWords) {
		double[] X = new double[K + wordsFeature.size()];
		List<Tmodel> topicParams = topicModelParam;
		int maxTopicId = 0;
		int keywordsThreshold = (int) (wordFeature.size() * 0.3);
		int keywordsSize = keywordsThreshold > 3 ? keywordsThreshold : 3;
		for (int i = 0; i < topicParams.size(); i++) {
			Tmodel model = topicParams.get(i);
			double pz = model.getPz();
			X[i] -= Math.log(pz);
			for (Iterator<Entry<Integer, Integer>> wordandfres = wordFeature
					.entrySet().iterator(); wordandfres.hasNext();) {
				Entry<Integer, Integer> wordandfre = wordandfres.next();
				Integer wordId = wordandfre.getKey();
				Integer fre = wordandfre.getValue();
				if (model.getPwz().containsKey(wordId)) {
					double pwz = model.getPwz().get(wordId);
					X[i] -= Math.log(pwz) * fre;
				}
			}
			if (X[i] > X[maxTopicId]) {
				maxTopicId = i;
			}
		}
		List<KeyWord> candidateKeywords = new ArrayList<KeyWord>();
		StringBuffer keywds = new StringBuffer();
		/**
		 * 生成 doc对应的keywords
		 */
		int counter = 0;
		for (Iterator<Entry<Integer, Integer>> wordandfres = wordFeature
				.entrySet().iterator(); wordandfres.hasNext();) {
			Entry<Integer, Integer> wordandfre = wordandfres.next();
			Integer wordId = wordandfre.getKey();
			Integer fre = wordandfre.getValue();
			if (keywords.containsKey(wordId)) {
				String wd = keywords.get(wordId);
				// 以下追加词 直接作为关键词
				if ("GDP".equals(wd.trim()) || "App".equals(wd.trim())
						|| "PE".equals(wd.trim()) || "私募债".equals(wd.trim())
						|| "p2p".equals(wd.trim()) || "众筹".equals(wd.trim())
						|| "VC".equals(wd.trim()) || "网购".equals(wd.trim())
						|| "O2O".equals(wd.trim()) || "运营商".equals(wd.trim())
						|| "网络安全".equals(wd.trim()) || "群聊".equals(wd.trim())
						|| "百度".equals(wd.trim()) || "移动业务".equals(wd.trim())
						|| "计算机病毒".equals(wd.trim()) || "大数据".equals(wd.trim())
						|| "二维码支付".equals(wd.trim()) || "支付宝".equals(wd.trim())
						|| "第三方支付".equals(wd.trim()) || "债基".equals(wd.trim())
						|| "支付行为".equals(wd.trim())) {
					// keyWords.append(wd).append(":").append("-1").append(",");
					// //加入关键词 对应的相关度
					keyWords.append(wd).append(",");
					counter++;
					continue;
				}
				double value = 0.0;
				for (int i = 0; i < X.length; i++) {
					if (X[maxTopicId] - X[i] < 0.25) {
						Tmodel model = topicParams.get(i);
						if (model.getPwz().containsKey(wordId)) {
							value += model.getPwz().get(wordId) * X[i];
						}
					}
				}
				value *= fre;
				KeyWord key = new KeyWord(wordId, value);
				candidateKeywords.add(key);
			}
		}
		Collections.sort(candidateKeywords);
		for (int i = candidateKeywords.size() - 1; i > 0; i--) {
			if (keywordsSize < counter) { // 关键词个数是否满足条件
				break;
			} else {
				KeyWord word = candidateKeywords.get(i);
				String wd = keywords.get(word.getName());
				keyWords.append(wd).append(",");
				counter++;
			}
		}
		/*
		 * if(keyWords!=null&&keyWords.length()>0){
		 * keyWords.apkeywds.substring(0,keywds.length()); }
		 */
		/**
		 * 加入词特称
		 */
		for (int i = 0; i < wordsFeature.size(); i++) {
			Integer key = wordsFeature.get(i);
			if (wordFeature.containsKey(key)) {
				X[K + i] = wordFeature.get(key);
			} else {
				X[K + i] = 0.0;
			}
		}
		return X;
	}

	/**
	 * 词特称-主题特称映射
	 * 
	 * @return
	 */
	public double[] words2Topic(Map<Integer, Integer> wordFeature) {
		double[] X = new double[K + wordsFeature.size()];
		List<Tmodel> topicParams = topicModelParam;
		for (int i = 0; i < topicParams.size(); i++) {
			Tmodel model = topicParams.get(i);
			double pz = model.getPz();
			X[i] -= Math.log(pz);
			for (Iterator<Entry<Integer, Integer>> wordandfres = wordFeature
					.entrySet().iterator(); wordandfres.hasNext();) {
				Entry<Integer, Integer> wordandfre = wordandfres.next();
				Integer wordId = wordandfre.getKey();
				Integer fre = wordandfre.getValue();
				if (model.getPwz().containsKey(wordId)) {
					double pwz = model.getPwz().get(wordId);
					X[i] -= Math.log(pwz) * fre;
				}
			}
		}
		/**
		 * 加入词特称
		 */
		for (int i = 0; i < wordsFeature.size(); i++) {
			Integer key = wordsFeature.get(i);
			if (wordFeature.containsKey(key)) {
				X[K + i] = wordFeature.get(key);
			} else {
				X[K + i] = 0.0;
			}
		}
		return X;
	}

	/**
	 * 类别推理
	 * 
	 * @param x
	 * @param y
	 */
	public double[] predict(double[] x, double[] y) {
		long starttime = System.currentTimeMillis();
		double[] Y = new double[n_out];
		for (int i = 0; i < n_out; i++) {
			Y[i] = 0;
			for (int j = 0; j < n_in; j++) {
				Y[i] += SoftmaxModelParam.getW()[i][j] * x[j];
			}
			Y[i] += SoftmaxModelParam.getB()[i];
		}
		softmax(Y);
		return Y;
	}

	public void softmax(double[] x) {
		double max = -Double.MAX_VALUE;
		double sum = 0.0;

		for (int i = 0; i < n_out; i++) {
			if (max < x[i]) {
				max = x[i];
			}
		}

		for (int i = 0; i < n_out; i++) {
			x[i] = Math.exp(x[i] - max);
			sum += x[i];
		}

		for (int i = 0; i < n_out; i++) {
			x[i] /= sum;
		}
	}

	public String classifier(String title, String content) {
		String classily = "";
		double[] X = null;
		StringBuffer doc = null;
		String text = null;
		Map<Integer, Integer> wordFeature = null;
		/**
		 * 数据清洗
		 */
		if (content != null) {
			doc = htmlFilter(content);
		}
		if (title != null) {
			int pos = title.lastIndexOf(">>");
			if (pos != -1 && pos < title.length())
				title = title.substring(pos);
			doc.append(title).append("\t").append(title).append("\t")
					.append(title);
		}
		/**
		 * 文本过滤 （噪声）
		 */
		if (doc != null && doc.length() > 0) {
			text = docFilter(doc.toString());
		}
		StringBuffer keyWords = new StringBuffer();
		/**
		 * 分词
		 */
		if (text != null && text.length() > 0) {
			wordFeature = cutWords(text);
			/**
			 * 词特称-主题特称映射
			 */
			if (wordFeature != null && wordFeature.size() != 0) {
				X = topicFeature(wordFeature, keyWords);
				/**
				 * 类别推理
				 */
				int index = -1;
				if (X != null) {
					double[] Y = predict(X, null);
					double maxvalue = Double.MIN_VALUE;
					for (int y = 0; y < Y.length; y++) {
						if (maxvalue < Y[y] && Y[y] > 0.0) {
							maxvalue = Y[y];
							index = y;
						}
					}
					classily = SoftmaxModelParam.getIndex2Class().get(index);
				} else {
					classily = "other";
				}
			} else {
				classily = "other";
			}
			keyWords.append("\t").append(classily);
		}
		return keyWords.toString();
	}

	/**
	 * dom tree 先序遍历
	 * 
	 * @param pRoot
	 * @param regex
	 * @return
	 */
	public StringBuffer travel(Element pRoot, String regex) {
		if (pRoot == null)
			return null;
		StringBuffer sf = new StringBuffer();
		Element lpNode = null;
		Stack<Element> stack = new Stack<Element>(); // 创建堆栈对象
		stack.push(pRoot);
		while (!stack.empty()) {
			lpNode = stack.pop();
			String content = lpNode.ownText().trim();
			if (content.length() != 0) {
				if (sf.length() > 0) { // 出版：
					if (content.matches(".*QQ 群:*.")
							|| content.matches(".*交流群：*.")
							|| content.matches(".*此文转发*.")
							|| content.matches(".*长期订阅*.")
							|| content.matches(".*微信：*.")
							|| content.matches(".*来自：*.")
							|| content.matches(".*微信公众号：*.")
							|| content.matches(".*加关注*.")
							|| content.matches(".*长按图片*.")
							|| content.matches(".*添加群：*.")
							|| content.matches(".*邮箱：*.")
							|| content.matches(".*欢迎交流 *.")
							|| content.matches(".*原文链接 *.")
							|| content.matches(".*长按二维码*.")
							|| content.matches(".*阅读原文*.")
							|| content.matches(".*来源:*.")
							|| content.matches(".*为作者独立观点*.")
							|| content.matches(".*联系方式：*.")
							|| content.matches(".*编辑:*.")
							|| content.matches(".*为感谢大家对本次活动的支持*.")
							|| content.matches(".*QQ:*.")
							|| content.matches(".*欢迎关注*.")
							|| content.matches(".*动漫周边：*.")
							|| content.matches(".*转载人：*.")
							|| content.matches(".*声明：*.")
							|| content.matches(".*电话：*.")
							|| content.matches(".*地址：*.")
							|| content.matches(".*TEL：*.")
							|| content.matches(".*加微信公众账号*.")
							|| content.matches(".*发稿:*.")
							|| content.matches(".*本文信息仅供参考*.")
							|| content.matches(".*作者：*.")
							|| content.matches(".*客服热线：*.")
							|| content.contains("微信号：")
							|| content.matches(".*联系方式*.")
							|| content.contains("微信订阅号：")
							|| content.contains("请关注：")
							|| content.contains("QQ群：")
							|| content.matches(".*稿源：*.")
							|| content.matches(".*更多精彩内容*.")
							|| content.matches(".*转发到：*.")
							|| content.matches(".*发稿:*.")
							|| content.matches(".*识别二维码*.")
							|| content.matches(".*请查看:*.")
							|| content.matches(".*作者：*.")
							|| content.matches(".*编辑：*.")
							|| content.matches(".*在本网站刊登的所有内容*.")
							|| content.matches(".*文章来源 *.")
							|| content.matches(".*网站运营： *.")) {
						continue;
					} else {
						sf.append(content).append("\t");
					}
				} else {
					sf.append(content).append("\t");
				}
			}

			for (int i = lpNode.children().size() - 1; i >= 0; i--) {
				Element element = lpNode.children().get(i);
				if (!"".equals(element.text().trim())) {
					stack.push(element);
				}
			}
		}
		return sf;
	}

	/**
	 * 文本过滤
	 * 
	 * @return
	 */
	public String docFilter(String doc) {
		// 您当前所处位置: 【大公网海南频道讯】 (周一) 原标题:摘要:□本报实习记者 □本报记者
		String text = doc;
		String regex1 = "(url:|http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
		String regex2 = "(\\[.{0,10}\\]|\\【.{0,10}\\】|\\(.{0,10}\\)|原标题:|摘要:|□本报实习记者|□本报记者|.*您当前所处位置:)";
		text = text.replaceAll(regex1, "");
		text = text.replaceAll(regex2, "");
		return text;
	}

	/**
	 * 数据清洗
	 * 
	 * @param html
	 * @return
	 */
	public StringBuffer htmlFilter(String html) {
		// 解析HTML字符串返回一个Document实现
		String regex = "(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
		Document doc = Jsoup.parseBodyFragment(html);
		Element body = doc.body();
		StringBuffer sf = travel(body, regex);
		return sf;
	}

	public LabelAndKeywords getBankTagNewssOpen(String title, String content) {
		if (content == null && title == null) {
			return null;
		} else {
			String classilyAndKeywords = classifier(title, content); // 分类器
			String[] wordsAndClassily = classilyAndKeywords.split("\t");
			String classily = wordsAndClassily[1];
			String keywds = wordsAndClassily[0];
			String[] kds = null;
			if (keywds != null) {
				kds = keywds.split(",");
			}
			LabelAndKeywords labelAndKeywords = new LabelAndKeywords(classily,
					kds);
			return labelAndKeywords;
		}
	}

}
