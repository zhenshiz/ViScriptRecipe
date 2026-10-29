package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.viscript_lib.gui.editor.IRuntimeFileProject;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeFile;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.gui.views.WorkBenchView;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RecipeProject implements IRuntimeFileProject {
    @Getter
    @Setter
    private RecipeFile recipeFile = new RecipeFile();
    @Getter
    private final Resources resources = Resources.of();

    @Nullable
    @Getter
    private Editor editor;
    @Nullable
    private NavigationView navigationView;
    @Nullable
    private WorkBenchView workbenchView;
    @Nullable
    private PropertiesView propertiesView;
    private boolean aiPreviewing;

    /**
     * 切换配方信息对象并重建对应视图，保留旧对象供已有历史引用。
     *
     * @param file 完整的配方文件数据
     */
    public void installAiFile(RecipeFile file) {
        var old = recipeFile;
        recipeFile = file;
        try { refreshAiViews(); }
        catch (RuntimeException exception) { recipeFile = old; refreshAiViews(); throw exception; }
    }

    /** 刷新领域视图，不记录历史。 */
    public void refreshAiViews() {
        if (editor == null) return;
        var currentEditor = editor;
        if (navigationView != null) navigationView.removeSelf();
        if (workbenchView != null) workbenchView.removeSelf();
        if (propertiesView != null) propertiesView.removeSelf();
        onLoad(currentEditor);
        if (!recipeFile.getEntries().isEmpty()) navigationView.selectEntry(recipeFile.getEntries().getFirst());
        setAiPreviewing(aiPreviewing);
    }

    /**
     * 暂停配方修改，仍允许通过左侧列表浏览预览。
     *
     * @param previewing 是否等待应用或放弃
     */
    public void setAiPreviewing(boolean previewing) {
        aiPreviewing = previewing;
        if (navigationView != null) navigationView.setAiPreviewing(previewing);
        if (workbenchView != null && workbenchView.getCanvas() != null) workbenchView.getCanvas().setActive(!previewing);
        if (propertiesView != null) propertiesView.setAiPreviewing(previewing);
    }

    @Override
    public ProjectType getProjectType() {
        return RecipeProjectType.TYPE;
    }

    @Override
    public void initNewProject() {
        recipeFile = new RecipeFile();
    }

    public void saveCurrentVisualState() {
        if (navigationView == null) return;
        navigationView.saveCanvas();
    }

    void selectEntry(RecipeEntry entry) {
        if (navigationView == null || !recipeFile.getEntries().contains(entry)) return;
        navigationView.selectEntry(entry);
    }

    @Override
    public CompoundTag serializeProject(@NotNull HolderLookup.Provider provider) {
        return serializeRuntimeFile(provider);
    }

    @Override
    public CompoundTag serializeRuntimeFile(@Nonnull HolderLookup.Provider provider) {
        saveCurrentVisualState();
        return recipeFile.serializeNBT(provider);
    }

    @Override
    public void deserializeProject(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag nbt) {
        recipeFile = new RecipeFile();
        recipeFile.deserializeNBT(provider, nbt);
    }

    @Override
    public void onLoad(@Nonnull Editor editor) {
        IRuntimeFileProject.super.onLoad(editor);
        this.editor = editor;
        this.navigationView = new NavigationView(this);
        this.workbenchView = new WorkBenchView(navigationView);
        this.propertiesView = new PropertiesView(navigationView, workbenchView);
        editor.placeView(navigationView, () -> editor.leftWindow.getLeftTop());
        editor.placeView(workbenchView, () -> editor.centerWindow.getLeftTop());
        editor.placeView(propertiesView, () -> editor.rightWindow.getRightTop());
    }

    @Override
    public void onClosed(@Nonnull Editor editor) {
        IRuntimeFileProject.super.onClosed(editor);
        if (navigationView != null) navigationView.removeSelf();
        if (workbenchView != null) workbenchView.removeSelf();
        if (propertiesView != null) propertiesView.removeSelf();
        this.editor = null;
        this.navigationView = null;
        this.workbenchView = null;
        this.propertiesView = null;
    }
}
