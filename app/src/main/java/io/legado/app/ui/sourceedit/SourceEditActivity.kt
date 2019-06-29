package io.legado.app.ui.sourceedit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import io.legado.app.R
import io.legado.app.base.BaseActivity
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.rule.*
import io.legado.app.utils.GSON
import io.legado.app.utils.getViewModel
import kotlinx.android.synthetic.main.activity_source_edit.*
import org.jetbrains.anko.toast

class SourceEditActivity : BaseActivity<SourceEditViewModel>() {
    override val viewModel: SourceEditViewModel
        get() = getViewModel(SourceEditViewModel::class.java)
    override val layoutID: Int
        get() = R.layout.activity_source_edit

    private val adapter = SourceEditAdapter()
    private val sourceEditList: ArrayList<EditEntity> = ArrayList()
    private val searchEditList: ArrayList<EditEntity> = ArrayList()
    private val findEditList: ArrayList<EditEntity> = ArrayList()
    private val infoEditList: ArrayList<EditEntity> = ArrayList()
    private val tocEditList: ArrayList<EditEntity> = ArrayList()
    private val contentEditList: ArrayList<EditEntity> = ArrayList()

    override fun onViewModelCreated(viewModel: SourceEditViewModel, savedInstanceState: Bundle?) {
        initView()
        viewModel.sourceLiveData.observe(this, Observer {
            upRecyclerView(it)
        })
        if (viewModel.sourceLiveData.value == null) {
            val sourceID = intent.getStringExtra("data")
            if (sourceID == null) {
                upRecyclerView(null)
            } else {
                sourceID.let { viewModel.setBookSource(sourceID) }
            }
        } else {
            upRecyclerView(viewModel.sourceLiveData.value)
        }
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.source_edit, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                val bookSource = getSource()
                if (bookSource == null) {
                    toast("书源名称和URL不能为空")
                } else {
                    viewModel.save(bookSource) { finish() }
                }
            }
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun initView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setEditEntities(tab?.position)
            }
        })
    }

    private fun setEditEntities(tabPosition: Int?) {
        when (tabPosition) {
            1 -> adapter.editEntities = searchEditList
            2 -> adapter.editEntities = findEditList
            3 -> adapter.editEntities = infoEditList
            4 -> adapter.editEntities = tocEditList
            5 -> adapter.editEntities = contentEditList
            else -> adapter.editEntities = sourceEditList
        }
    }

    private fun upRecyclerView(bookSource: BookSource?) {
        bookSource?.let {
            cb_is_enable.isChecked = it.enabled
            cb_is_enable_find.isChecked = it.enabledExplore
        }
        sourceEditList.clear()
        sourceEditList.add(EditEntity("bookSourceUrl", bookSource?.bookSourceUrl, R.string.book_source_url))
        sourceEditList.add(EditEntity("bookSourceName", bookSource?.bookSourceName, R.string.book_source_name))
        sourceEditList.add(EditEntity("bookSourceGroup", bookSource?.bookSourceGroup, R.string.book_source_group))
        sourceEditList.add(EditEntity("loginUrl", bookSource?.loginUrl, R.string.book_source_login_url))
        sourceEditList.add(EditEntity("header", bookSource?.header, R.string.source_http_header))
        //搜索
        with(bookSource?.getSearchRule()) {
            searchEditList.clear()
            searchEditList.add(EditEntity("url", this?.url, R.string.rule_search_url))
            searchEditList.add(EditEntity("bookList", this?.bookList, R.string.rule_book_list))
            searchEditList.add(EditEntity("name", this?.name, R.string.rule_book_name))
            searchEditList.add(EditEntity("author", this?.author, R.string.rule_book_author))
            searchEditList.add(EditEntity("kind", this?.kind, R.string.rule_book_kind))
            searchEditList.add(EditEntity("wordCount", this?.wordCount, R.string.rule_word_count))
            searchEditList.add(EditEntity("lastChapter", this?.lastChapter, R.string.rule_last_chapter))
            searchEditList.add(EditEntity("intro", this?.intro, R.string.rule_book_intro))
            searchEditList.add(EditEntity("coverUrl", this?.coverUrl, R.string.rule_content_url))
            searchEditList.add(EditEntity("bookUrl", this?.bookUrl, R.string.rule_book_url))
        }
        //详情页
        with(bookSource?.getBookInfoRule()) {
            infoEditList.clear()
            infoEditList.add(EditEntity("urlPattern", this?.urlPattern, R.string.book_url_pattern))
            infoEditList.add(EditEntity("init", this?.init, R.string.rule_book_info_init))
            infoEditList.add(EditEntity("name", this?.name, R.string.rule_book_name))
            infoEditList.add(EditEntity("author", this?.author, R.string.rule_book_author))
            infoEditList.add(EditEntity("coverUrl", this?.coverUrl, R.string.rule_cover_url))
            infoEditList.add(EditEntity("intro", this?.intro, R.string.rule_book_intro))
            infoEditList.add(EditEntity("kind", this?.kind, R.string.rule_book_kind))
            infoEditList.add(EditEntity("wordCount", this?.wordCount, R.string.rule_word_count))
            infoEditList.add(EditEntity("lastChapter", this?.lastChapter, R.string.rule_last_chapter))
            infoEditList.add(EditEntity("tocUrl", this?.tocUrl, R.string.rule_toc_url))
        }
        //目录页
        with(bookSource?.getTocRule()) {
            tocEditList.clear()
            tocEditList.add(EditEntity("chapterList", this?.chapterList, R.string.rule_chapter_list))
            tocEditList.add(EditEntity("chapterName", this?.chapterName, R.string.rule_chapter_name))
            tocEditList.add(EditEntity("chapterUrl", this?.chapterUrl, R.string.rule_content_url))
            tocEditList.add(EditEntity("nextTocUrl", this?.nextTocUrl, R.string.rule_next_toc_url))
        }
        //正文页
        with(bookSource?.getContentRule()) {
            contentEditList.clear()
            contentEditList.add(EditEntity("content", this?.content, R.string.rule_book_content))
            contentEditList.add(EditEntity("nextContentUrl", this?.nextContentUrl, R.string.rule_content_url_next))
        }

        //发现
        with(bookSource?.getExploreRule()) {
            findEditList.clear()
            findEditList.add(EditEntity("url", this?.url, R.string.rule_find_url))
            findEditList.add(EditEntity("bookList", this?.bookList, R.string.rule_book_list))
            findEditList.add(EditEntity("name", this?.name, R.string.rule_book_name))
            findEditList.add(EditEntity("author", this?.author, R.string.rule_book_author))
            findEditList.add(EditEntity("kind", this?.kind, R.string.rule_book_kind))
            findEditList.add(EditEntity("wordCount", this?.wordCount, R.string.rule_word_count))
            findEditList.add(EditEntity("intro", this?.intro, R.string.rule_book_intro))
            findEditList.add(EditEntity("lastChapter", this?.lastChapter, R.string.rule_last_chapter))
            findEditList.add(EditEntity("coverUrl", this?.coverUrl, R.string.rule_cover_url))
            findEditList.add(EditEntity("bookUrl", this?.bookUrl, R.string.rule_book_url))
        }
        adapter.editEntities = sourceEditList
        adapter.notifyDataSetChanged()
    }

    private fun getSource(): BookSource? {
        val source = BookSource()
        source.enabled = cb_is_enable.isChecked
        source.enabledExplore = cb_is_enable_find.isChecked
        viewModel.sourceLiveData.value?.let {
            source.customOrder = it.customOrder
            source.weight = it.weight
        }
        val searchRule = SearchRule()
        val exploreRule = ExploreRule()
        val bookInfoRule = BookInfoRule()
        val tocRule = TocRule()
        val contentRule = ContentRule()
        for (entity in sourceEditList) {
            with(entity) {
                when (key) {
                    "bookSourceUrl" -> if (value != null) source.bookSourceUrl = value!! else return null
                    "bookSourceName" -> if (value != null) source.bookSourceName = value!! else return null
                    "bookSourceGroup" -> source.bookSourceGroup = value
                    "loginUrl" -> source.loginUrl = value
                    "header" -> source.header = value
                }
            }
        }
        for (entity in searchEditList) {
            with(entity) {
                when (key) {
                    "url" -> searchRule.url = value
                    "searchList" -> searchRule.bookList = value
                    "searchName" -> searchRule.name = value
                    "searchAuthor" -> searchRule.author = value
                    "searchKind" -> searchRule.kind = value
                    "searchIntro" -> searchRule.intro = value
                    "updateTime" -> searchRule.updateTime = value
                    "wordCount" -> searchRule.wordCount = value
                    "lastChapter" -> searchRule.lastChapter = value
                    "coverUrl" -> searchRule.coverUrl = value
                    "bookUrl" -> searchRule.bookUrl = value
                }
            }
        }
        for (entity in findEditList) {
            with(entity) {
                when (key) {
                    "url" -> exploreRule.url = value
                    "searchList" -> exploreRule.bookList = value
                    "searchName" -> exploreRule.name = value
                    "searchAuthor" -> exploreRule.author = value
                    "searchKind" -> exploreRule.kind = value
                    "searchIntro" -> exploreRule.intro = value
                    "updateTime" -> exploreRule.updateTime = value
                    "wordCount" -> exploreRule.wordCount = value
                    "lastChapter" -> exploreRule.lastChapter = value
                    "coverUrl" -> exploreRule.coverUrl = value
                    "bookUrl" -> exploreRule.bookUrl = value
                }
            }
        }
        for (entity in infoEditList) {
            with(entity) {
                when (key) {
                    "urlPattern" -> bookInfoRule.urlPattern = value
                    "init" -> bookInfoRule.init = value
                    "searchName" -> bookInfoRule.name = value
                    "searchAuthor" -> bookInfoRule.author = value
                    "searchKind" -> bookInfoRule.kind = value
                    "searchIntro" -> bookInfoRule.intro = value
                    "updateTime" -> bookInfoRule.updateTime = value
                    "wordCount" -> bookInfoRule.wordCount = value
                    "lastChapter" -> bookInfoRule.lastChapter = value
                    "coverUrl" -> bookInfoRule.coverUrl = value
                    "tocUrl" -> bookInfoRule.tocUrl = value
                }
            }
        }
        for (entity in infoEditList) {
            with(entity) {
                when (key) {
                    "urlPattern" -> bookInfoRule.urlPattern = value
                    "init" -> bookInfoRule.init = value
                    "searchName" -> bookInfoRule.name = value
                    "searchAuthor" -> bookInfoRule.author = value
                    "searchKind" -> bookInfoRule.kind = value
                    "searchIntro" -> bookInfoRule.intro = value
                    "updateTime" -> bookInfoRule.updateTime = value
                    "wordCount" -> bookInfoRule.wordCount = value
                    "lastChapter" -> bookInfoRule.lastChapter = value
                    "coverUrl" -> bookInfoRule.coverUrl = value
                    "tocUrl" -> bookInfoRule.tocUrl = value
                }
            }
        }
        for (entity in tocEditList) {
            with(entity) {
                when (key) {
                    "chapterList" -> tocRule.chapterList = value
                    "chapterName" -> tocRule.chapterName = value
                    "chapterUrl" -> tocRule.chapterUrl = value
                    "nextTocUrl" -> tocRule.nextTocUrl = value
                }
            }
        }
        for (entity in contentEditList) {
            with(entity) {
                when (key) {
                    "content" -> contentRule.content = value
                    "nextContentUrl" -> contentRule.nextContentUrl = value
                }
            }
        }
        source.ruleSearch = GSON.toJson(searchRule)
        source.ruleExplore = GSON.toJson(exploreRule)
        source.ruleBookInfo = GSON.toJson(bookInfoRule)
        source.ruleToc = GSON.toJson(tocRule)
        source.ruleContent = GSON.toJson(contentRule)
        setEditEntities(tab_layout.selectedTabPosition)
        return source
    }

    class EditEntity(var key: String, var value: String?, var hint: Int)
}