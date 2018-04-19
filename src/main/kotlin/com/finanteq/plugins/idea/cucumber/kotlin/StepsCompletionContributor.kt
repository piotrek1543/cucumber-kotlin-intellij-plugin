package com.finanteq.plugins.idea.cucumber.kotlin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.kotlin.idea.util.projectStructure.module
import org.jetbrains.plugins.cucumber.completion.CucumberPrefixMatcherExposer
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinFileType

class StepsCompletionContributor : CompletionContributor() {

    init {

        val inStep = psiElement().inside(psiElement().withElementType(GherkinElementTypes.STEP))
        extend(CompletionType.BASIC, inStep, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
                addStepDefinitions(result, parameters.originalFile)
            }
        })
    }

    private fun addStepDefinitions(result: CompletionResultSet, file: PsiFile) {

        val prefix = result.prefixMatcher.prefix
        val resultSet = result.withPrefixMatcher(CucumberPrefixMatcherExposer(prefix))
        val project = file.project

        val module = file.module!!
        val fileIndex = ModuleRootManager.getInstance(module).fileIndex
        val moduleContentScope = module.moduleContentScope
        fileIndex.iterateContent(ContentIterator {

            if (it.fileType is GherkinFileType) {

                val gherkinFile = it.toPsiFile(project) as? GherkinFile

                gherkinFile?.also {
                    it.features.forEach {
                        it.scenarios.forEach {
                            it.steps.forEach {
                                val stepName = it.stepName
                                if (stepName != prefix) {
                                    resultSet.addElement(LookupElementBuilder.create(stepName))
                                }
                            }
                        }
                    }
                }
            }

            true
        }, moduleContentScope)
    }


}