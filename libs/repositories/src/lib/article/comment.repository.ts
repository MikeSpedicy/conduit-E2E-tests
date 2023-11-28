import { Injectable, OnModuleInit } from '@nestjs/common';
import { mapping } from 'cassandra-driver';
import { CassandraService } from '@conduit/cassandra-service';
import { Comment } from './models/comment.model';
// eslint-disable-next-line @typescript-eslint/no-var-requires
const testSelectorsList = require('E2ETests/TestSelectorsList.json');

@Injectable()
export class CommentRepository implements OnModuleInit {
  private isIncludeIssues = testSelectorsList.isIncludeIssues;
  constructor(private cassandraService: CassandraService) {}

  commentMapper: mapping.ModelMapper<Comment>;

  onModuleInit() {
    const mappingOptions: mapping.MappingOptions = {
      models: {
        Comments: {
          tables: ['comments'],
          mappings: new mapping.UnderscoreCqlToCamelCaseMappings(),
        },
      },
    };

    this.commentMapper = this.cassandraService
      .createMapper(mappingOptions)
      .forModel('Comments');
  }

  async getByArticle(article) {
    const query = `SELECT * FROM comments WHERE article = '${article}' ALLOW FILTERING`;
    const response = await this.cassandraService.client.execute(query);

    return response?.rows;
  }

  create(comment) {
    return this.commentMapper.insert(comment);
  }

  async remove(id) {
    if (this.isIncludeIssues) {
      return this.commentMapper.remove({ id });
    } else {
      const query = `DELETE FROM comments WHERE id = '${id}';`;
      const response = await this.cassandraService.client.execute(query);
      return response;
    }
  }
}
