import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditorComponent } from './components/editor/editor.component';
import { ArticleViewComponent } from './components/article-view/article-view.component';
import { RouterModule } from '@angular/router';
import { articleRoutes } from './article.routes';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [EditorComponent, ArticleViewComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(articleRoutes)
  ],
})
export class ArticleModule { }
