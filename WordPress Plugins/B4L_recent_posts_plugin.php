<?php
/**
 * @package B4L_RecentPosts
 * @author Andrew Regan
 * @version 1.0
 */
/*
Plugin Name: Bloggers4Labour Recent Posts
Plugin URI: http://www.bloggers4labour.org/#
Description: Displays the most recent posts from the Bloggers4Labour network
Author: Andrew Regan
Version: 1.0
Author URI: http://www.bloggers4labour.org/
*/

function B4L_displayRecentPosts( $inNumPosts )
{
	$theNumPosts = ( $inNumPosts < 1) ? 0 : ( $inNumPosts > 20 ? 20 : $inNumPosts);
	if ( $theNumPosts == 0)
	{
		return;
	}

	echo "<div id=\"b4l-recentPosts08\" style=\"display: none;\">";
		echo "<p class=\"recentPosts08-head\"><span class=\"recentPosts08-head\">Last <span id=\"recent_posts_count_08\"></span> Posts @ <span id=\"recent_posts_date\">current time</span>...&nbsp;</span></p>";
		echo "<div class=\"feeds-box\" style=\"border:0\">";
			echo "<div id=\"search_posts_section\">";
				echo "<div id=\"search_posts_posts\"></div>";
			echo "</div>";
		echo "</div>";
	echo "</div>";

	$url = "http://www.bloggers4labour.org/";

	echo "<script type=\"text/javascript\">";
		echo "function updateRecentPosts() { refreshRecentPosts('" . $url . "'," . $theNumPosts . "); document.getElementById(\"b4l-recentPosts08\").style.display = 'block'; };";

		$theMethod = "updateRecentPosts";

		echo "if (window.attachEvent) {window.attachEvent('onload'," . $theMethod . "() );}";
		echo "else if (window.addEventListener) {window.addEventListener('load'," . $theMethod . ",false);}";
		echo "else {document.addEventListener('load'," . $theMethod . ",false);}";
	echo "</script>";
}

function B4L_widget_init()
{
	function B4L_InstallStyles()
	{
		echo "<script type=\"text/javascript\" src=\"http://www.bloggers4labour.org/js/main.js\"></script>\n";
		echo "<link rel=\"stylesheet\" href=\"http://www.bloggers4labour.org/css/tempBlogStyles.css\" type=\"text/css\" />\n";
	}

	function B4L_widget_register()
	{
//		wp_print_scripts( array('sack'));

//		$options = get_option('widget_twitter');
/*		$dims = array('width' => 300, 'height' => 300);
		$class = array('classname' => 'widget_twitter');

		for ($i = 1; $i <= 9; $i++) {
			$name = sprintf(__('Twitter #%d'), $i);
			$id = "twitter-$i"; // Never never never translate an id
			wp_register_sidebar_widget($id, $name, $i <= $options['number'] ? 'widget_twitter' :  '', $class, $i);
			wp_register_widget_control($id, $name, $i <= $options['number'] ? 'widget_twitter_control' : '', $dims, $i);
		}
	
		wp_register_sidebar_widget($id, $name, 'B4L_widget', $class, 1);
		wp_register_widget_control($id, $name, 'B4L_widget_control', $dims, 1);

		add_action('sidebar_admin_setup', 'widget_twitter_setup');
		add_action('sidebar_admin_page', 'widget_twitter_page');
 */	}

	B4L_widget_register();

	add_action('wp_head', 'B4L_InstallStyles');
}

add_action('widgets_init', 'B4L_widget_init');

?>
