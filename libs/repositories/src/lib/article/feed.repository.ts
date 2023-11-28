import { Injectable, OnModuleInit } from '@nestjs/common';
import { mapping } from 'cassandra-driver';
import { CassandraService } from '@conduit/cassandra-service';
import { Feed } from './models/feed.model';
// eslint-disable-next-line @typescript-eslint/no-var-requires
const testSelectorsList = require('E2ETests/TestSelectorsList.json');

@Injectable()
export class FeedRepository implements OnModuleInit {
  private isIncludeIssues = testSelectorsList.isIncludeIssues;

  constructor(private cassandraService: CassandraService) {}

  feedMapper: mapping.ModelMapper<Feed>;

  onModuleInit() {
    const mappingOptions: mapping.MappingOptions = {
      models: {
        Articles: {
          tables: ['articles'],
          mappings: new mapping.UnderscoreCqlToCamelCaseMappings(),
        },
      },
    };

    this.feedMapper = this.cassandraService
      .createMapper(mappingOptions)
      .forModel('Articles');
  }

  create(article) {
    return this.feedMapper.insert(article);
  }

  async getByID(id: string) {
    return await (await this.feedMapper.find({ id })).first();
  }

  updateArticle(article) {
    return this.feedMapper.update(article);
  }

  async getAll() {
    return await (await this.feedMapper.findAll()).toArray();
  }

  async getByAuthor(email: string) {
    const res = await this.cassandraService.client.execute(
      `SELECT * FROM articles WHERE author = '${email}' ALLOW FILTERING`
    );

    return res?.rows;
  }

  async delete(id, title) {
    if (this.isIncludeIssues) {
      return this.feedMapper.remove({ id, title });
    } else {
      const query = `DELETE FROM articles WHERE id = '${id}';`;
      const response = await this.cassandraService.client.execute(query);
      return response;
    }
  }
}
