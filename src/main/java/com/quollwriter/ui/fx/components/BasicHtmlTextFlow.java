package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;

import org.jsoup.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class BasicHtmlTextFlow extends TextFlow
{

    private StringProperty textProp = null;
    private URLActionHandler urlActionHandler = null;

    private BasicHtmlTextFlow (Builder b)
    {

        final BasicHtmlTextFlow _this = this;

        this.textProp = new SimpleStringProperty ();
        this.urlActionHandler = b.urlActionHandler;

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.setText (b.text);
        this.textProp.addListener ((p, oldv, newv) ->
        {

            _this.update ();

        });

        this.update ();

    }

    public StringProperty textProperty ()
    {

        return this.textProp;

    }

    public void setText (StringProperty text)
    {

        this.textProp.unbind ();

        if (text == null)
        {

            return;

        }

        this.textProp.bind (text);

    }

    public void setText (String text)
    {

        this.textProp.unbind ();
        this.textProp.setValue (text);

    }

    private void update ()
    {

        final BasicHtmlTextFlow _this = this;

        ObservableList<Node> children = this.getChildren ();

        children.clear ();

        String text = this.textProp.getValue ();

        if ((text == null)
            ||
            (text.length () == 0)
           )
        {

            return;

        }

        org.jsoup.nodes.Document doc = Jsoup.parse (text);

        for (org.jsoup.nodes.Node n : doc.body ().childNodes ())
        {

            if (n instanceof org.jsoup.nodes.TextNode)
            {

                org.jsoup.nodes.TextNode tn = (org.jsoup.nodes.TextNode) n;
                String t = tn.text ();

                // TODO Expand, consider using: https://code.i-harness.com/en/q/f2c1b
                t = StringUtils.replaceString (t,
                                               "&nbsp;",
                                               "\u00A0");

                children.add (new Text (t));

            }

            if (n instanceof org.jsoup.nodes.Element)
            {

                org.jsoup.nodes.Element el = (org.jsoup.nodes.Element) n;

                if (el.tagName ().equalsIgnoreCase ("br"))
                {

                    Text _t = new Text ("\n");
                    children.add (_t);
                    continue;

                }

                Text t = new Text (el.ownText ());
                t.getStyleClass ().add (el.tagName ());
                children.add (t);

                if (el.tagName ().equalsIgnoreCase ("a"))
                {

                    t.getStyleClass ().add (StyleClassNames.A);

                    t.getStyleClass ().add (el.attributes ().get ("class"));

                    //Hyperlink _t = new Hyperlink (el.ownText ());
                    //_t.setWrapText (true);
                    t.setOnMouseClicked (ev ->
                    {

                        String url = el.attributes ().get ("href");

                        try
                        {

                            UIUtils.openURL (this.urlActionHandler,
                                             null,
                                             url);

                        } catch (Exception e) {

                            Environment.logError ("Unable to browse to: " +
                                                  url,
                                                  e);

                            AbstractViewer viewer = Environment.getFocusedViewer ();

                            ComponentUtils.showErrorMessage (viewer,
                                                             getUILanguageStringProperty (Arrays.asList (general,unabletoopenwebpage),
                                                                                          url));
                                                      //"Unable to open web page: " + url);

                        }

                    });

                }

            }

        }

    }

    /**
     * Get a builder to create a new AccordionItem.
     *
     * Usage: AccordionItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static BasicHtmlTextFlow.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, BasicHtmlTextFlow>
    {

        private StringProperty text = null;
        private URLActionHandler urlActionHandler = null;
        private String styleName = null;

        private Builder ()
        {

        }

        @Override
        public BasicHtmlTextFlow build ()
        {

            return new BasicHtmlTextFlow (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder withHandler (URLActionHandler v)
        {

            this.urlActionHandler = v;
            return this;

        }

        public Builder text (String text)
        {

            this.text = new SimpleStringProperty (text);
            return this;

        }

        public Builder text (StringProperty text)
        {

            this.text = text;
            return this;

        }

    }

}