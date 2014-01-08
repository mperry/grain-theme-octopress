package com.example.grain.taglib

import com.sun.xml.internal.ws.util.StringUtils
import com.sysgears.grain.taglib.GrainTagLib

class OctopressTagLib {

    /**
     * Grain taglib reference.
     */
    private GrainTagLib taglib

    public OctopressTagLib(GrainTagLib taglib) {
        this.taglib = taglib
    }

    /**
     * Renders a quote block which contains the quote text, author and source.
     *
     * @attr content the quote content
     * @attr author (optional) quote author
     * @attr sourceTitle (optional) title of the quote source
     * @athr sourceLink (optional) link to the quote source
     */
    def blockquote = { Map model = null ->
        // validates the tag attributes
        if (!model.content) throw new IllegalArgumentException('Tag [blockquote] is missing required attribute [content]')

        taglib.include('/tags/blockquote.html', [quote: model])
    }

    /**
     * Renders a content and duplicates the quote withing the content with a different formatting style.
     * <br />
     * The quote must be surrounded by '{/' '/}' tags, for instance: 'Lorem ipsum {/dolor/} sit amet'.
     *
     * @attr content content that contains a quote
     * @attr align (optional) quote position, can be either 'right' or 'left', 'right' is by default
     */
    def pullquote = { model ->
        // validates the tag attributes
        if (!model.content) throw new IllegalArgumentException('Tag [pullquote] is missing required attribute [content]')

        String content = model.content
        String align = model.align ?: 'right'

        // finds quote which is surrounded by '{/' '/}' tags
        String quote = content.find(/\{\/(.*)\/\}/) { match, quote -> quote }
        // removes '{/' '/}' tags from the content
        content = content.replace('{/', '').replace('/}', '')

        taglib.include('/tags/pullquote.html', [textblock: [content: content, quote: quote, align: align]])
    }

    /**
     * Embeds a gist into the page.
     *
     * @attr id unique gist identifier
     */
    def gist = { Map model = null ->
        // validates the tag attributes
        if (!model.id) throw new IllegalArgumentException('Tag [gist] is missing required attribute [id]')

        taglib.include('/tags/gist.html', [gist: model])
    }

    /**
     * Generates html tag for an image
     *
     * @attr location image location
     * @attr width (optional) image width
     * @attr height (optional) image height
     */
    def img = { String location, Integer width = null, Integer height = null ->
        def widthStr = width ? " width=\"${width}\"" : ""
        def heightStr = height ? " height=\"${height}\"" : ""

        "<img${widthStr}${heightStr} src=\"${taglib.r(location)}\" alt=\"image\">"
    }

    /**
     * Embeds a video into the page.
     *
     * @attr urls links to the videos
     * @attr poster (optional) link to a poster
     * @attr wight (optional) video wight
     * @attr height (optional) video height
     */
    def video = { Map model = null ->

        // validates the tag attributes
        if (!model.urls || !(model.urls instanceof List) || model.urls.isEmpty()) {
            throw new IllegalArgumentException('Tag [video] is missing required attribute [urls]')
        }

        def types = ['mp4': 'video/mp4',
                'ogv': 'video/ogg',
                'webm': 'video/webm'] // supported video types

        def videoSources = []

        model.urls.each {
            def type = types."${it.find(/[^\.]+$/)}"
            if (type) {
                videoSources << [url: it, type: type]
            }
        }

        if (videoSources.isEmpty()) {
            throw new IllegalArgumentException("Tag [video] does not support file formats of any of the provided video sources")
        }

        model << [sources: videoSources]

        taglib.include('/tags/video.html', [video: model])
    }

    /**
     * Converts title by applying Title Case capitalizing convention (capitalizes all principal words).
     *
     * @attr title the title to convert
     *
     * @return title-case string
     */
    static def titleCase = { String title ->
        def nonPrincipalWords = ['a', 'an', 'and', 'as', 'at', 'but', 'by', 'en', 'for', 'if', 'in',
                'of', 'on', 'or', 'the', 'to', 'v', 'v.', 'via', 'vs', 'vs.']
        title.split(' ').inject(new StringBuilder()) {result, word ->
            word in nonPrincipalWords ? result.append(word) : result.append(StringUtils.capitalize(word))
            result.append(' ')
        } .toString().trim()
    }

    /**
     * Converts a date to XML date time format.
     *
     * @attr date the date to convert
     *
     * @return XML date time representation of the date, for instance 2013-12-31T12:49:00+07:00
     */
    static def xmlDateTime = { Date date ->
        def tz = String.format('%tz', date)
        String.format("%tFT%<tT${tz.substring(0, 3)}:${tz.substring(3)}", date)
    }
}
