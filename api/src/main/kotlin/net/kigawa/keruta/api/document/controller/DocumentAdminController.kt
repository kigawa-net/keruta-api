package net.kigawa.keruta.api.document.controller

import net.kigawa.keruta.core.domain.model.Document
import net.kigawa.keruta.core.usecase.document.DocumentService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/documents")
class DocumentAdminController(
    private val documentService: DocumentService,
) {

    @GetMapping
    fun documentList(model: Model): String {
        model.addAttribute("pageTitle", "Document Management")
        model.addAttribute("documents", documentService.getAllDocuments())
        return "admin/documents"
    }

    @GetMapping("/create")
    fun createDocumentForm(model: Model): String {
        model.addAttribute("pageTitle", "Create Document")
        model.addAttribute(
            "document",
            Document(
                title = "",
                content = "",
                tags = emptyList(),
            ),
        )
        return "admin/document-form"
    }

    @PostMapping("/create")
    fun createDocument(@ModelAttribute document: Document): String {
        documentService.createDocument(document)
        return "redirect:/admin/documents"
    }

    @GetMapping("/edit/{id}")
    fun editDocumentForm(@PathVariable id: String, model: Model): String {
        try {
            val document = documentService.getDocumentById(id)
            model.addAttribute("pageTitle", "Edit Document")
            model.addAttribute("document", document)
            return "admin/document-form"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/documents"
        }
    }

    @PostMapping("/edit/{id}")
    fun updateDocument(@PathVariable id: String, @ModelAttribute document: Document): String {
        try {
            documentService.updateDocument(id, document)
        } catch (e: NoSuchElementException) {
            // Document not found, ignore
        }
        return "redirect:/admin/documents"
    }

    @GetMapping("/delete/{id}")
    fun deleteDocument(@PathVariable id: String): String {
        try {
            documentService.deleteDocument(id)
        } catch (e: NoSuchElementException) {
            // Document not found, ignore
        }
        return "redirect:/admin/documents"
    }
}
