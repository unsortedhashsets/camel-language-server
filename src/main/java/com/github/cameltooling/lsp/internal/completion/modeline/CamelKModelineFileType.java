/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.cameltooling.lsp.internal.completion.modeline;

import org.eclipse.lsp4j.CompletionItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Provided configuration utils associated to File Types when handling modeline
 *
 * @author joshiraez
 */
public enum CamelKModelineFileType {
    XML(
            List.of(".camelk.xml"),
            "<!-- camel-k:",
            CamelKModelineFileType::textIsFullyCommentedXML,
            "<!-- camel-k: -->",
            "Read more: https://camel.apache.org/camel-k/1.9.x/cli/modeline.html"),
    Java(
            List.of(".java"),
            "// camel-k:",
            CamelKModelineFileType::textIsFullyCommentedJava,
            "// camel-k:",
            "Read more: https://camel.apache.org/camel-k/1.9.x/cli/modeline.html"),
    YAML(
            List.of(".camelk.yaml",".camelk.yml"),
            "# camel-k",
            CamelKModelineFileType::textIsFullyCommentedYAML,
            "# camel-k",
            "Read more: https://camel.apache.org/camel-k/1.9.x/cli/modeline.html");

    public final List<String> correspondingExtensions;
    public final String modeline;
    public final Function<String, Boolean> checkTextIsCommentsDelegate;

    public final CompletionItem completion;

    CamelKModelineFileType(List<String> correspondingExtensions,
                           String modeline,
                           Function<String, Boolean> checkTextIsCommentsDelegate,
                           String completionLabel,
                           String completionDocumentation) {
        this.correspondingExtensions = correspondingExtensions;
        this.modeline = modeline;
        this.checkTextIsCommentsDelegate = checkTextIsCommentsDelegate;
        this.completion = getCompletionItem(completionLabel, completionDocumentation);
    }

    public static CompletionItem getCompletionItem(String label, String documentation) {
        CompletionItem completion = new CompletionItem(label);
        completion.setDocumentation(documentation);

        return completion;
    }

    public static Optional<CamelKModelineFileType> getFileTypeCorrespondingToUri(String uri) {
        return List.of(CamelKModelineFileType.values()).stream()
                .filter(type ->
                        type.correspondingExtensions.stream().anyMatch(uri::endsWith)
                )
                .findFirst();
    }

    private static boolean textIsFullyCommentedXML(String text){
        //Remove all segments between <!-- and -->. Check if it's empty.
        Pattern commentRegex = Pattern.compile("<!--.*-->");

        return textIsFullOfRegex(text, commentRegex);
    }

    private static boolean textIsFullyCommentedYAML(String text){
        //Remove all segments between # and \n
        Pattern commentRegex = Pattern.compile("#.*\\n");

        return textIsFullOfRegex(text, commentRegex);
    }

    private static boolean textIsFullyCommentedJava(String text){
        //Line Comments: Remove from // to \n
        //Block Comments: Remove from /* to */. Newlines have to be explicitly added
        Pattern lineComment = Pattern.compile("\\/\\/.*\\n");
        Pattern blockComment = Pattern.compile("\\/\\*(.|\\n)*\\*\\/");
        Pattern commentRegex = Pattern.compile(String.format("(%s|%s)",lineComment.pattern(), blockComment.pattern()));

        return textIsFullOfRegex(text, commentRegex);
    }

    private static boolean textIsFullOfRegex(String text, Pattern regex) {
        //Add an extra carriage return at the end for correct matching with line comments
        String textWithExtraCarriageReturn = text + "\n";
        String textWithoutComments = textWithExtraCarriageReturn.replaceAll(regex.pattern(), "");

        return textWithoutComments.isBlank();
    }
}