import { Injectable } from '@nestjs/common';
import { Client, mapping, auth } from 'cassandra-driver';

@Injectable()
export class CassandraService {
  client: Client;
  mapper: mapping.Mapper;

  private createClient() {
    // Local Connection
    // this.client = new Client({
    //   contactPoints: ['localhost'],
    //   keyspace: 'conduit',
    //   localDataCenter: 'datacenter1',
    //   authProvider: new auth.PlainTextAuthProvider('cassandra', 'cassandra'),
    // });

    // Astra connection
    this.client = new Client({
      // changes in lines with the ignore comment will not be sent to the repo if you make such configuration locally -
      // `git config filter.ignore-line.clean "sed '/\/\/ ignore-line/d'"`;
      // `git config filter.ignore-line.smudge "your value"`;
      // `git config filter.ignore-line.required true`
      cloud: {
        secureConnectBundle:
          // Astra's DB secure-connect data
      },
      credentials: {
        // Astra's clientId value
        username:
          //
        // Astra's secret value
        password:
          //
      },
      // keyspace of your DB
      keyspace:
        //
    });
  }

  createMapper(mappingOptions: mapping.MappingOptions) {
    if (this.client == undefined) {
      this.createClient();
      this.client.connect();
    }
    return new mapping.Mapper(this.client, mappingOptions);
  }
}
