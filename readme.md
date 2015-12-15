##说明



给定标题和内容，得到标签和关键词

基于ansj,对该作者 阿健表示感谢！！！

##项目地址：

https://github.com/liangyangtao/classily.git

##使用实例

>
{% highlight java %}
   public static void main(String[] args) {
		ClassifierService classifierService = new ClassifierService();
		classifierService.init();
		String title = "千山药机布局精准医疗打造“慢病管理生态圈”全貌首次呈现";
		String content = "29318947	<div>  <div>  <p>食药监总局日前发布《药品不良反应信息通报》，提示关注非典型抗精神病药的严重不良反应。非典型抗精神病药可引起白细胞减少，严重时可致粒细胞缺乏症，使机体对病原体的防御能力降低，导致感染甚至危及生命。还可引起血脂升高、体重增加，从而引发心脑血管疾病；或引起血糖升高或糖尿病，严重可致酮症酸中毒、高渗性昏迷或死亡。</p>  <p>据介绍，非典型抗精神病药主要用于急、慢性精神分裂症及其他各种精神病性状态的阳性症状和阴性症状的治疗。</p> </div></div>";

		System.out.println(classifierService
				.getBankTagNewssOpen(title, content));

	}
	
{% endhighlight %}

执行结果：

>
{% highlight java %}

LabelAndKeywords{label='医疗医药', keywords=[管理, 精神病, 症状, 山药, 生态圈, 医疗, 心脑血管, 疾病]}

{% endhighlight %}
